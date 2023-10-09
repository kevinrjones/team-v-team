# team-v-team

This project generates HTML pages [such as these](https://archive.acscricket.com/records_and_stats/team_v_team_wbbl/index.html). The json files used to drive the project are in the 'data_full' directory and the output will be placed into a directory specified in the code.


## Running

run --args="-bd /Users/kevinjones/Dropbox/projects/cricket/team-by-team/data -c jdbc:mysql://localhost:3306/cricketarchive?useSSL=true&requireSSL=true -u cricketarchive -p p4ssw0rd  -o /Users/kevinjones/Dropbox/team_v_team"