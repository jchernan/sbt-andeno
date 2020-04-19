#!/bin/bash
out_dir=/home/ubuntu
deploy_dir=/srv/$APP
echo "Starting app $APP"
nohup $deploy_dir/scripts/run.sh $APP $PORT > \
  $out_dir/$APP.out 2> $out_dir/$APP.err < /dev/null &
