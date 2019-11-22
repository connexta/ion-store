def NETWORK = "cdr"
def STACK = "store-stack"

def checkDir() {
    def dir = System.properties['user.dir']
    def composeFile = new File(dir,"deploy.groovy")
    if (!composeFile.exists()){
        println "Please run in the same directory as the deploy.groovy script."
        System.exit(1)
    }
}

def run(commands) {
    println "Running: " + commands
    def proc = commands.execute()
    proc.waitForProcessOutput(System.out, System.err)
}

def header(message) {
    println ""
    println "# # # # # # # # # # # # # #"
    println " " + message
    println ""
}

def createLocalS3Store() {
    header("Deleting contents of .localstack")
    def tmpDir = new File("../../../.localstack")
    if (tmpDir.exists()) {
        tmpDir.deleteDir()
    }
    tmpDir.mkdir()
}

header("Checking directory...")
checkDir()
header("Deploying the application on " + STACK)
createLocalS3Store()
run("docker stack rm " + STACK)
run("docker network create --driver=overlay --attachable " + NETWORK)
run("docker stack deploy -c ../../../docker-compose.yml -c ../docker-override.yml " + STACK)
run("docker stack services " + STACK)
