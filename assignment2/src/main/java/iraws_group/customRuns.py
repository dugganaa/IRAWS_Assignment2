import os

JAR_LOC = "target/assignment2-1.0.SNAPSHOT.jar"
DIR_VAL = "2"
JM_VAL = "3"
LAMBDA_VAL = "0.1f"

index_dir_command = JAR_LOC + " " + str(1) + " " + DIR_VAL + " " + LAMBDA_VAL
stream = os.popen(index_dir_command)
output = stream.read()
print(output)
