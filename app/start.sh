#!/bin/sh
bash ./gradlew -t classes &
bash ./gradlew -t bootRun -PskipDownload=true
