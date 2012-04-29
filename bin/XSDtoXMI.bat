@echo off
setlocal
SET CP=%~dp0\NRL\antlr-runtime-3.2.jar;%~dp0\NRL\nrl.jar;%~dp0\NRL\nrl-interpreter.jar
SET CP=%CP%;%~dp0\XML\resolver.jar;%~dp0\XML\serializer.jar;%~dp0\XML\xalan.jar;%~dp0\XML\xercesImpl.jar;%~dp0\XML\xml-apis.jar
SET CP=%~dp0\mdmiMapEditor.jar;%~dp0\mdmi.jar;%CP%

java -cp %CP% org.openhealthtools.mdht.mdmi.model.syntax.XSDtoXMI %1 %2 %3 %4 %5 %6 %7 %8 %9