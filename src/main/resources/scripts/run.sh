#!/bin/bash
app=$1
port=$2
deploy_dir=/srv/$app
env_file=/home/ubuntu/env.sh
pid_file=/home/ubuntu/$app.pid
port_arg=`[[ ! -z "$port" ]] && echo "-Dhttp.port=$port" || echo ""`
command="$deploy_dir/bin/$app -Dpidfile.path=$pid_file $port_arg"

cleanup() {
  echo "$(timestamp) - Cleaning up app $app"
  if [ -f $pid_file ]; then
    kill -9 $(cat $pid_file)
    rm $pid_file
  fi
}

run() {
  echo "$(timestamp) - Running app $app"
  if [ -f $env_file ]; then
    . $env_file
  fi
  until `eval $command`; do
    echo "$(timestamp) - $app crashed with exit code $?. Respawning ..." >&2
    cleanup
    sleep 1
  done
}

timestamp() {
  date +"%Y-%m-%d %H:%M:%S,%3N"
}

trap cleanup EXIT
run
