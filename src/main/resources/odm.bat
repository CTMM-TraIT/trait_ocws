@ECHO OFF
REM	Copyright 2012-2013 VU Medical Center Amsterdam
REM
REM	Licensed under the Apache License, Version 2.0 (the "License");
REM	you may not use this file except in compliance with the License.
REM	You may obtain a copy of the License at
REM
REM	    http://www.apache.org/licenses/LICENSE-2.0
REM
REM	Unless required by applicable law or agreed to in writing, software
REM	distributed under the License is distributed on an "AS IS" BASIS,
REM	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM	See the License for the specific language governing permissions and
REM	limitations under the License.

REM Wrapper for ODM export and import
REM Requires java 1.6 build 30

REM 2012-2013, VU Medical Center Amsterdam
REM Author: Arjan van der Velde 
REM Author: Jacob Rousseau

set "OC_URL="
set "OC_USER="
set "OC_PASSWORD="
set "OC_STUDY="

IF "%2" == "-b" (
    set OC_URL=-b %~3
)
IF "%4" == "-u" (
    set OC_USER=-u %~5
)
IF "%6" == "-p" (
    set OC_PASSWORD=-p %~7
)

IF "%8" == "-s" (
    set OC_STUDY=-s %~9
)
echo OC_URL is: %OC_URL%
echo OC_USER is: %OC_USER%
echo OC_PASSWORD is: %OC_PASSWORD%
echo OC_STUDY is: %OC_STUDY%
REM Windows requires ';' as JAR delimeter in multiple jars in the class-path 
REM while Unix needs a ':' 
set JARFILE=project.build.jarName;log4j.jar;commons-cli.jar
echo Classpath is: %JARFILE%

REM determine action
IF "X%1%" == "X--extract" (
	REM extract
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.ExtractODM %OC_URL% %OC_USER% %OC_PASSWORD%  %OC_STUDY%
 ) ELSE ( 
 IF "X%1%" == "X--import" (
	REM import
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.ImportODM %OC_URL% %OC_USER% %OC_PASSWORD% 
 ) ELSE ( 
 IF "X%1%" == "X--subjects" (
	REM subjects
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.ListSubjects %OC_URL% %OC_USER% %OC_PASSWORD% %OC_STUDY%
 ) ELSE ( 
 IF  "X%1%" == "X--clean" (
	REM clean
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.CleanODM %OC_URL% %OC_USER% %OC_PASSWORD% 
 ) ELSE ( 
 IF "X%1%" == "X--studies" (
	REM studies
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.ListStudies %OC_URL% %OC_USER% %OC_PASSWORD%
) ELSE (
	echo "Usage: `basename $0` < --extract | --import | --subjects | --clean | --studies > [ -h | --help ] <command specific options>"
)))))

set "OC_URL="
set "OC_USER="
set "OC_PASSWORD="
set "OC_STUDY="

