#!/bin/bash

#################################
# Used to search ldap for users #
#################################

username=$1 #login as this user
searchuser=$2 #user to search for

ldapsearch -h pdc.westmont.edu -b cn=Users,dc=campus,dc=westmont,dc=edu -D cn=$username,cn=Users,dc=campus,dc=westmont,dc=edu -W cn=$searchuser
