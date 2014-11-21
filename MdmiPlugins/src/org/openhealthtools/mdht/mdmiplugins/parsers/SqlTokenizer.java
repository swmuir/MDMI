package org.openhealthtools.mdht.mdmiplugins.parsers;

import java.util.*;

public class SqlTokenizer {
   public ArrayList<Token> tokenize( String sql ) {
      ArrayList<Token> tokens = new ArrayList<Token>();
      if( null == sql || sql.trim().length() <= 0 )
         return tokens;
      StringBuffer sb = new StringBuffer(sql.trim());
      Token t = null;
      while( null != (t = getNextToken(sb)) ) {
         tokens.add(t);
      }
      return tokens;
   }
   
   private static final String WHITE_SPACE = " \t\r\n";
   private static final String DELIMS = "[\"\'";
   private static final String DIGITS = "0123456789.";
   private static final String SYMBOLS = "=.*()_%,";
   
   private Token getNextToken( StringBuffer sb ) {
      if( sb.length() <= 0 )
         return null;
      char c =  sb.charAt(0);
      while( 0 <= WHITE_SPACE.indexOf(c) ) {
         sb = sb.deleteCharAt(0);
         if( sb.length() <= 0 )
            return null;
         c =  sb.charAt(0);
      }
      // first non-white space
      if( 0 <= DELIMS.indexOf(c) ) {
         // delimiter, so we have a literal
         char e = c;
         if( '[' == c )
            e = ']';
         String value = getDelimitedString(sb, c, e);
         if( '[' == c )
            return new TWord(value);
         return new TLiteral(value);
      }
      else if( 0 <= DIGITS.indexOf(c) ) {
         // number
         char n = c;
         StringBuffer s = new StringBuffer();
         while( 0 <= DIGITS.indexOf(n) ) {
            s.append(n);
            sb = sb.deleteCharAt(0);
            if( sb.length() <= 0 )
               break;
            n = sb.charAt(0);
         }
         return new TLiteral(s.toString());
      }
      else if( Character.isLetter(c) ) {
         // word
         char n = c;
         StringBuffer s = new StringBuffer();
         while( Character.isLetterOrDigit(n) ) {
            s.append(n);
            sb = sb.deleteCharAt(0);
            if( sb.length() <= 0 )
               break;
            n = sb.charAt(0);
         }
         return new TLiteral(s.toString());
      }
      else if( 0 <= SYMBOLS.indexOf(c) ) {
         sb = sb.deleteCharAt(0);
         return new TSymbol("" + c);
      }
      throw new RuntimeException("Unknown/unsupported token in '" + sb.toString() + "'");
   }
   
   private String getDelimitedString( StringBuffer sb, char sd, char ed ) {
      if( sd != sb.charAt(0) )
         throw new RuntimeException("Invalid delimiter in '" + sb.toString() + "'");
      if( sb.length() < 2 )
         throw new RuntimeException("Unmatched delimiter in '" + sb.toString() + "'");
      String orig = sb.toString();
      sb = sb.deleteCharAt(0); // first one is always sd
      if( sb.length() == 1 && ed == sb.charAt(0) ) {
         sb = sb.deleteCharAt(0);
         return "";
      }
      
      StringBuffer s = new StringBuffer();
      char c = sb.charAt(0);
      boolean edFound = false;
      do {
         if( ed == c ) {
            sb = sb.deleteCharAt(0);
            if( ed == sd ) {
               // check for '' and ""
               if( 0 < sb.length() ) {
                  char d = sb.charAt(0);
                  if( c == d ) {
                     s.append(c);
                     sb = sb.deleteCharAt(0);
                  }
                  else
                     edFound = true;
               }
            }
            else 
               edFound = true;
         }
         else {
            s.append(c);
            sb = sb.deleteCharAt(0);
            if( sb.length() <= 0 )
               throw new RuntimeException("Unmatched delimiter in '" + orig + "'");
            c = sb.charAt(0);
         }
      } while( 0 < sb.length() && !edFound );
      
      return s.toString();
   }
   
   public static void main( String[] args ) {
      SqlTokenizer st = new SqlTokenizer();
      ArrayList<Token> tokens = st.tokenize("SELECT T.a, T.b FROM Table T WHERE T.id = '1'");
      System.out.println(tokens);
   }
   
   public static abstract class Token {
      public String value;
      
      @Override
      public String toString() {
         return value;
      }
   }
   
   public static class TWord extends Token {
      public TWord( String value ) {
         super.value = value;
      }
   }
   
   public static class TLiteral extends Token {
      public TLiteral( String value ) {
         super.value = value;
      }
   }
   
   public static class TSymbol extends Token {
      public TSymbol( String value ) {
         super.value = value;
      }
   }
} // SqlTokenizer
