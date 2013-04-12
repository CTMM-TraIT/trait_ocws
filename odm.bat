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

set JARFILE=traitocws-0.0.4-SNAPSHOT.jar:log4j.jar:commons-cli.jar
echo Classpath is: %JARFILE%
IF NOT "X%OC_URL%" == "X" (
	set OC_URL=-b"$OC_URL"
)
IF NOT "X%OC_USER%" == "X" (
	set OC_USER=-u"$OC_USER"
)
IF NOT "X%OC_PASSWORD%" == "X" (
	set OC_PASSWORD=-p"$OC_PASSWORD"
)

echo OC_URL is: %OC_URL%

REM determine action
IF "X%1%" == "X--extract" (
	REM extract
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.ExtractODM "$OC_URL" "$OC_USER" "$OC_PASSWORD" "$@"
 ) ELSE ( 
 IF "X%1%" == "X--import" (
	REM import
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.ImportODM "$OC_URL" "$OC_USER" "$OC_PASSWORD" "$@"
 ) ELSE ( 
 IF "X%1%" == "X--subjects" (
	REM subjects
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.ListSubjects "$OC_URL" "$OC_USER" "$OC_PASSWORD" "$@"
 ) ELSE ( 
 IF  "X%1%" == "X--clean" (
	REM clean
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.CleanODM "$OC_URL" "$OC_USER" "$OC_PASSWORD" "$@"
 ) ELSE ( 
 IF "X%1%" == "X--studies" (
	REM studies
	shift
	java -cp %JARFILE% nl.vumc.trait.oc.main.ListStudies "$OC_URL" "$OC_USER" "$OC_PASSWORD" "$@"
) ELSE (
	echo Usage: `basename $0` ^< --extract | --import | --subjects | --clean | --studies ^> ^[ -h | --help ^] ^<command specific options^>
)))))
