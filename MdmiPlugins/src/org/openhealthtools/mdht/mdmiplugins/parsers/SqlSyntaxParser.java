package org.openhealthtools.mdht.mdmiplugins.parsers;

import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.engine.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;
import org.openhealthtools.mdht.mdmiplugins.parsers.SqlTokenizer.*;

public class SqlSyntaxParser implements ISyntacticParser {
   ArrayList<Token> tokens;
   
   @Override
   public ISyntaxNode parse( MessageModel mdl, MdmiMessage msg ) {
      if( mdl == null || msg == null )
         throw new IllegalArgumentException("Null argument!");
      byte[] data = msg.getData();
      if( data == null )
         return null; // <---- NOTE message can be empty
      
      String sql = null;
      try {
         sql = new String(data, "UTF-8");
      }
      catch( Exception ex ) {
         throw new RuntimeException("Invalid message bytes, or not encoded in UTF-8!", ex);
      }
      SqlTokenizer tokenizer = new SqlTokenizer();
      tokens = tokenizer.tokenize(sql);
      if( !ensureIsSelect() )
         throw new RuntimeException("Invalid message format, must be a SELECT statement, starting with the SELECT keyword!");

      YBag yroot = null;
      try {
         MessageSyntaxModel syn = mdl.getSyntaxModel();
         Node node = syn.getRoot();
         if( !(node instanceof Bag) )
            throw new MdmiException("Root node error expected a bag, found {0}", node.getClass().getName());
         Bag root = (Bag)node;
         yroot = new YBag(root, null);
         ArrayList<Node> nodes = root.getNodes();
         if( 4 != nodes.size() )
            throw new MdmiException(
                  "Invalid model for this type of message, need 4 bags: FIELDS, JOINS, WHERE, and SORT, found {0}"
                  , nodes.size());
         // Select FIELDS
         Bag bfields = (Bag)nodes.get(0);
         YBag yfields = new YBag(bfields, yroot);
         yroot.addYNode(yfields);
         readFields(yfields, bfields);

         Bag bfrom = (Bag)nodes.get(1);
         YBag yfrom = new YBag(bfrom, yroot);
         yroot.addYNode(yfrom);
         readFrom(yfrom, bfrom);
           
         Bag bwhere = (Bag)nodes.get(2);
         YBag ywhere = new YBag(bwhere, yroot);
         if( readWhere(ywhere, bwhere) )
            yroot.addYNode(ywhere);

         Bag bsort = (Bag)nodes.get(3);
         YBag ysort = new YBag(bsort, yroot);
         if( readSort(ysort, bsort) )
            yroot.addYNode(ysort);
      }
      catch( MdmiException ex ) {
         throw ex;
      }
      catch( Exception ex ) {
         throw new MdmiException(ex, "Syntax.parse(): unexpected exception");
      }
      return yroot;
   }

   @Override
   public void serialize( MessageModel mdl, MdmiMessage msg, ISyntaxNode root ) {
      // TODO Auto-generated method stub
      
   }

   private boolean ensureIsSelect() {
      if( null == tokens || tokens.size() <= 1 )
         return false;
      Token t = tokens.remove(0);
      return (t instanceof TWord) && t.value.equalsIgnoreCase("SELECT");
   }
   
   // *, or T.*, or T.f, T.b, ...
   private void readFields( YBag yfields, Bag bfields ) {
      String field = getField();
      while( null != field ) {
         LeafSyntaxTranslator lft = (LeafSyntaxTranslator)bfields.getNode("FIELD");
         YLeaf yf = new YLeaf(lft, yfields, field);
         yfields.addYNode(yf);
         field = getField();
      }
   }
   private String getField() {
      Token t = tokens.get(0);
      if( (t instanceof TWord) && t.value.equalsIgnoreCase("FROM") )
         return null; // done
      if( !(t instanceof TWord) || t.value.equals("*") )
         throw new MdmiException("Invalid syntax, expected field found " + t);
      tokens.remove(0);
      String f = t.value;
      t = tokens.get(0);
      if( (t instanceof TWord) && t.value.equalsIgnoreCase("FROM") )
         return f; // done

      if( (t instanceof TSymbol) && t.value.equals(",") ) {
         tokens.remove(0); // remove comma
         return f; // done
      }
      
      if( (t instanceof TSymbol) && t.value.equals(".") ) {
         if( f.equals("*") )
            throw new MdmiException("Invalid syntax, unexpected token '.' after " + f);
         f += ".";
         tokens.remove(0);
         t = tokens.get(0);
         if( (t instanceof TWord) && t.value.equalsIgnoreCase("FROM") )
            throw new MdmiException("Invalid syntax, missing field name, found FROM after " + f);
         if( !(t instanceof TWord) || t.value.equals("*") )
            throw new MdmiException("Invalid syntax, expected field name after " + f);
         f += t.value;
         tokens.remove(0);
         t = tokens.get(0);
         if( (t instanceof TSymbol) && t.value.equals(",") )
            tokens.remove(0); // remove comma
         return f; // done
      }
      throw new MdmiException("Invalid syntax, unexpected token " + t);
   }

   // either FROM T[ A] or
   // FROM T1[ A], T2[ B], ...
   // FROM T1[ A] [LEFT|RIGHT] [INNER] JOIN T2[ B] ON [A.|T1.]k1 = [B.|T2.]k2 JOIN... 
   private void readFrom( YBag yfrom, Bag bfrom ) {
      Token t = tokens.remove(0);
      if( !(t instanceof TWord) || !t.value.equalsIgnoreCase("FROM") )
         throw new MdmiException("Invalid syntax, expected FROM, found " + t);
      t = tokens.remove(0);
      if( !(t instanceof TWord) )
         throw new MdmiException("Invalid syntax, expected table name, found " + t);
      T table = new T(t.value);
      t = tokens.get(0);
      if( t.value.equalsIgnoreCase("WHERE") || t.value.equalsIgnoreCase("ORDER") ) {
         addTable(yfrom, bfrom, table);
         return; // done
      }
      if( t.value.equals(",") ) {
         // FROM T1[ A], T2[ B], ...
         ArrayList<T> tables = new ArrayList<T>();
         tables.add(table);
         tokens.remove(0);
         while( null != (table = getNextTable()) ) {
            tables.add(table);
         }
         for( int i = 0; i < tables.size(); i++ ) {
            addTable(yfrom, bfrom, tables.get(i));
         }
         return; // done
      }
      // if we get here we have joins
      ArrayList<J> joins = new ArrayList<J>();
      J join = null;
      while( null != (join = getNextJoin()) ) {
         joins.add(join);
      }
      addTable(yfrom, bfrom, table);
      for( int i = 0; i < joins.size(); i++ ) {
         addJoin(yfrom, bfrom, joins.get(i));
      }
   }
   private void addTable( YBag yfrom, Bag bfrom, T table ) {
      Bag bt = (Bag)bfrom.getNode("TABLE");
      YBag yt = new YBag(bt, yfrom);
      yfrom.addYNode(yt);
      LeafSyntaxTranslator lft = (LeafSyntaxTranslator)bt.getNode("NAME");
      YLeaf yf = new YLeaf(lft, yt, table.name);
      yt.addYNode(yf);
      if( null != table.alias ) {
         lft = (LeafSyntaxTranslator)bt.getNode("ALIAS");
         yf = new YLeaf(lft, yt, table.alias);
         yt.addYNode(yf);
      }
   }
   private void addJoin( YBag yfrom, Bag bfrom, J join ) {
      Bag bj = (Bag)bfrom.getNode("JOIN");
      YBag yj = new YBag(bj, yfrom);
      yfrom.addYNode(yj);
      Bag bt = (Bag)bj.getNode("TABLE");
      YBag yt = new YBag(bt, yfrom);
      yj.addYNode(yt);
      LeafSyntaxTranslator lft = (LeafSyntaxTranslator)bt.getNode("NAME");
      YLeaf yf = new YLeaf(lft, yt, join.table.name);
      yt.addYNode(yf);
      if( null != join.table.alias ) {
         lft = (LeafSyntaxTranslator)bt.getNode("ALIAS");
         yf = new YLeaf(lft, yt, join.table.alias);
         yt.addYNode(yf);
      }
      
      lft = (LeafSyntaxTranslator)bj.getNode("LEFTKEY");
      yf = new YLeaf(lft, yj, join.leftKey);
      yj.addYNode(yf);
      lft = (LeafSyntaxTranslator)bj.getNode("RIGHTKEY");
      yf = new YLeaf(lft, yj, join.rightKey);
      yj.addYNode(yf);
      if( join.isInner ) {
         lft = (LeafSyntaxTranslator)bj.getNode("INNER");
         yf = new YLeaf(lft, yj, "true");
         yj.addYNode(yf);
      }
      if( join.isOuter ) {
         lft = (LeafSyntaxTranslator)bj.getNode("OUTER");
         yf = new YLeaf(lft, yj, "true");
         yj.addYNode(yf);
      }
      if( join.isLeft ) {
         lft = (LeafSyntaxTranslator)bj.getNode("LEFT");
         yf = new YLeaf(lft, yj, "true");
         yj.addYNode(yf);
      }
      if( join.isRight ) {
         lft = (LeafSyntaxTranslator)bj.getNode("RIGHT");
         yf = new YLeaf(lft, yj, "true");
         yj.addYNode(yf);
      }
   }
   private T getNextTable() {
      Token t = tokens.get(0);
      if( t.value.equalsIgnoreCase("WHERE") || t.value.equalsIgnoreCase("ORDER") )
         return null; // done
      if( !(t instanceof TWord) )
         throw new MdmiException("Invalid syntax, expected table name found " + t);
      tokens.remove(0);
      T table = new T(t.value);
      t = tokens.get(0);
      if( t.value.equalsIgnoreCase("WHERE") || t.value.equalsIgnoreCase("ORDER") )
         return table; // done

      if( (t instanceof TSymbol) && t.value.equals(",") ) {
         tokens.remove(0); // remove comma
         return table; // done
      }
      
      if( (t instanceof TWord) ) {
         // alias
         tokens.remove(0); // remove alias
         table.alias = t.value;
         t = tokens.get(0);
         if( t.value.equalsIgnoreCase("WHERE") || t.value.equalsIgnoreCase("ORDER") )
            return table; // done
         if( (t instanceof TSymbol) && t.value.equals(",") )
            tokens.remove(0); // remove comma
         return table; // done
      }
      throw new MdmiException("Invalid syntax, unexpected token " + t);
   }
   private J getNextJoin() {
      Token t = tokens.get(0);
      if( t.value.equalsIgnoreCase("WHERE") || t.value.equalsIgnoreCase("ORDER") )
         return null; // done
      if( !(t instanceof TWord) )
         throw new MdmiException("Invalid syntax, expected table name found " + t);
      J join = new J();
      do {
         t = tokens.remove(0);
         if( t.value.equalsIgnoreCase("INNER") )
            join.isInner = true;
         if( t.value.equalsIgnoreCase("OUTER") )
            join.isOuter = true;
         if( t.value.equalsIgnoreCase("LEFT") )
            join.isLeft = true;
         if( t.value.equalsIgnoreCase("RIGHT") )
            join.isRight = true;
         if( !t.value.equalsIgnoreCase("JOIN") )
            throw new MdmiException("Invalid syntax, expected INNER, OUTER, LEFT, RIGHT or JOIN, found " + t);
      } while( !t.value.equalsIgnoreCase("JOIN") );

      t = tokens.remove(0);
      if( !(t instanceof TWord) )
         throw new MdmiException("Invalid syntax, expected table name found " + t);
      T table = new T(t.value);
      
      t = tokens.remove(0);
      if( !t.value.equalsIgnoreCase("ON") ) {
         // alias
         table.alias = t.value;
         t = tokens.remove(0);
      }
      if( !t.value.equalsIgnoreCase("ON") )
         throw new MdmiException("Invalid syntax, expected ON, found " + t);

      t = tokens.remove(0);
      if( !(t instanceof TWord) )
         throw new MdmiException("Invalid syntax, expected FIELD, found " + t);
      String f = t.value;
      t = tokens.remove(0);
      if( t.value.equals(".") ) {
         f += ".";
         t = tokens.remove(0);
         if( !(t instanceof TWord) )
            throw new MdmiException("Invalid syntax, expected FIELD.FIELD, found " + t);
         f += t.value;
      }
      join.leftKey = f;

      t = tokens.remove(0);
      if( t.value.equals("=") )
         throw new MdmiException("Invalid syntax, expected =, found " + t);

      t = tokens.remove(0);
      if( !(t instanceof TWord) )
         throw new MdmiException("Invalid syntax, expected FIELD, found " + t);
      f = t.value;
      t = tokens.remove(0);
      if( t.value.equals(".") ) {
         f += ".";
         t = tokens.remove(0);
         if( !(t instanceof TWord) )
            throw new MdmiException("Invalid syntax, expected FIELD.FIELD, found " + t);
         f += t.value;
      }
      join.rightKey = f;
      return join;
   }
   
   private boolean readWhere( YBag ywhere, Bag bwhere ) {
      return false;
   }

   private boolean readSort( YBag ysort, Bag bsort ) {
      return false;
   }
   
   static class T {
      public String name;
      public String alias;
      
      public T( String name ) {
         this.name = name;
      }

      public T( String name, String alias ) {
         super();
         this.name = name;
         this.alias = alias;
      }
   }
   
   static class J {
      public T table;
      public String leftKey;
      public String rightKey;
      public boolean isInner;
      public boolean isOuter;
      public boolean isLeft; 
      public boolean isRight; 
   }
} // SqlSyntaxParser
