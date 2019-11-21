def NETWORK = "cdr"
def STACK = "store-stack"

def checkVars() {
    def dir = System.properties['user.dir']
    def composeFile = new File(dir,"docker-compose.yml")
    if (!composeFile.exists()){
        println "Please run in the same directory as a docker-compose.yml"
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

header("Deploying the application on " + STACK)
run("docker stack rm " + STACK)
run("docker network create --driver=overlay --attachable " + NETWORK)
run("docker stack deploy -c ../../docker-compose.yml -c docker-override.yml " + STACK)
run("docker stack services " + STACK)
