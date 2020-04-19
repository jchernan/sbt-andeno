#!/bin/bash
echo "Stopping app $APP"
pkill -f "run.sh $APP"
exit 0
