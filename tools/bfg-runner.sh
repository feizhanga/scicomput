#!/bin/bash

#https://rtyley.github.io/bfg-repo-cleaner/
#https://help.github.com/articles/remove-sensitive-data/

# 1) git clone --mirror git@github.com:feizhanga/dirtyrepo.git
# 2) cleanup: ex, remove top 2 biggest blog example: java -jar bfg-1.12.8.jar -B 2 dirtyrepo.git
# 3) cd dirty.git ;  git reflog expire --expire=now --all && git gc --prune=now --aggressive
# 4) git push

java -jar bfg-1.12.8.jar $options 

