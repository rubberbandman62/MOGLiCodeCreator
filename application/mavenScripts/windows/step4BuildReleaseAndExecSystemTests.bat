echo off
echo *
echo *
echo *
echo *
echo *     ***********************************************************
echo *     ***********************************************************
echo *     ***********************************************************
echo *     *****                                                 *****
echo *     *****                      Step 4:                    *****
echo *     *****                Build release and                *****
echo *     *****               execute system tests              *****
echo *     *****                                                 *****
echo *     ***********************************************************
echo *     ***********************************************************
echo *     ***********************************************************
echo *
echo *
echo *
echo *

cd ..\..
call mvn clean install test -Dtest=BuildReleaseAndTestSystem
pause