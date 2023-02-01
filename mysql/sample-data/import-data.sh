#!/usr/bin/env bash

set -e

cd /sample-data

echo "IMPORTING DATA"
echo

mysql --host=database --port=3306 --database=employees --user=root --password=password < employees.sql

echo
echo "ALL DONE! EMPLOYEE COUNT IS BELOW"
echo
mysql --host=database --port=3306 --database=employees --user=dbuser --password=password --execute='select count(*) from employees;'
