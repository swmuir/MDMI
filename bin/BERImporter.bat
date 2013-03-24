@echo off
setlocal
SET CP=NRL\antlr-runtime-3.2.jar;NRL\nrl.jar;NRL\nrl-interpreter.jar
SET CP=%CP%;XML\resolver.jar;XML\serializer.jar;XML\xalan.jar;XML\xercesImpl.jar;XML\xml-apis.jar
SET CP=mdmiMapEditor.jar;mdmi.jar;%CP%

java -cp %CP% org.openhealthtools.mdht.mdmi.editor.ber_import.BERImporter %1 %2 %3 %4 %5 %6 %7 %8 %9