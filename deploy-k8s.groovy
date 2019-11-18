#!/usr/bin/env groovy
//Groovy scripts throw lots of WARNING messages, but it is fine
//https://issues.apache.org/jira/browse/GROOVY-8339

def DOCKER_REG = System.getenv("K8S_DOCKER_REGISTRY") + "/"
def DOCKER_IMAGE_NAME = "cnxta/ion-store"
def K8S_CONTEXT = getKubeContext()


def getKubeContext() {
    return "kubectl config current-context".execute().text.trim()
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

header("Tagging images...")
run("docker tag " + DOCKER_IMAGE_NAME + " " + DOCKER_REG + DOCKER_IMAGE_NAME)

if (K8S_CONTEXT != "minikube") {
    header("Pushing image to remote registry")
    run("docker push " + DOCKER_REG + DOCKER_IMAGE_NAME)
}

header("Creating configMaps for S3 and Store Service...")
run("kubectl create configmap s3-config-map --from-file=./configs/s3_config.yml")
run("kubectl create configmap store-config-map --from-file=./.k8s/store_config.yml")
run("kubectl create configmap transform-config-map --from-file=./configs/transform_config.yml")

header("Creating secrets for S3")
run("kubectl create secret generic s3-access-secret --from-file=./s3_access.sec")
run("kubectl create secret generic s3-secret-secret --from-file=./s3_secret.sec")

header("Deploying...")
run("kubectl apply -f ./.k8s/store-deployment.yml")
