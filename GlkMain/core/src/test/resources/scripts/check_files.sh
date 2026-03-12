#!/bin/bash

## begin configuration
username=gleichklang
password=gleichklang
db_name=gleichklang
db_url=localhost
file_path=./
## end configuration

while [[ $# -gt 1 ]]
  do
  key="$1"
  case $key in
    -h)
      db_url="$2"
      shift
      ;;
    -f)
      file_path="$2"
      shift
      ;;
    -db)
      db_name="$2"
      shift
      ;;
    *)
      #unknown
      ;;
  esac
  shift
  done

export username
export password
export db_name
export db_url
export file_path

find "$file_path" -type f -exec sh -c '
  for file do
    file_name=$(echo "$file" | sed -r "s:.*/::g")
    file_id=$(echo "$file" | sed -r "s:.*/([0-9]*)/.*:\1:g")
    found=$(echo -e "SELECT 1 FROM file WHERE id = $file_id AND name = \x22$file_name\x22" | mysql -u${username} -p${password} -h${db_url} ${db_name} | tail -n +2)
    if [ -z $found ]; then
      echo no db entry for $file
    fi
  done' find-sh {} +

echo "SELECT id, name FROM file" | mysql -u${username} -p${password} -h${db_url} ${db_name} | tail -n +2 | sed "s/\\t/\\//g" | sed -e "s:^:${file_path}:" | while read -r file
do
  if [ ! -f "$file" ]; then
    file_id=$(echo "$file" | sed -r 's:.*/([0-9]*)/.*:\1:g')
    user_id=$(echo "SELECT user_id FROM avatar WHERE file_id = $file_id" | mysql -u${username} -p${password} -h${db_url} ${db_name} | tail -n +2 | sed -r 's/\t.*//g')
    if [ -z "$user_id" ]; then
      user_id=$(echo "SELECT author_id FROM media JOIN media_gallery ON media_gallery_id = media_gallery.id WHERE file_id = $file_id" | mysql -u${username} -p${password} -h${db_url} ${db_name} | tail -n +2 | sed -r 's/\t.*//g')
    fi
    user_mail="not found"
    if [ -n "$user_id" ]; then
      user_mail=$(echo "SELECT email FROM user_ WHERE id = $user_id" | mysql -u${username} -p${password} -h${db_url} ${db_name} | tail -n +2 | sed -r 's/\t.*//g')
    fi
    echo $file missing from user: $user_mail
  fi
done