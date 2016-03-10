#!/bin/bash

#https://rtyley.github.io/bfg-repo-cleaner/
#https://help.github.com/articles/remove-sensitive-data/

# 1) git clone --mirror git@github.com:feizhanga/dirtyrepo.git
# 2) cleanup: ex, remove top 2 biggest blog example: java -jar bfg-1.12.8.jar -B 2 dirtyrepo.git
# 3) cd dirty.git ;  git reflog expire --expire=now --all && git gc --prune=now --aggressive
# 4) git push

echo "$# arguments receieved:   $@" 

if [ $#  -le 2 ]; then
  echo "********************************************************"
  echo "*  Usage   $0"
  echo "*  Usage Example: $0 -B 2 dirtyrepo.git  "
  echo "*"
  echo "* ******************************************************"
  exit 1
else
  java -jar bfg-1.12.8.jar "$@"
fi


