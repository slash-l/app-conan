 def server
 def buildInfo
 def conanClient
 String resolveRepo
 String deployRepo
 
 node("master") {
    stage("SCM"){
        // Clone the code from github:
        git url :'git@github.com:slash-l/app-conan.git' , branch : "main"
    }

    stage("Artifactory Configure"){
        // Obtain an Artifactory server instance, defined in Jenkins --> Manage Jenkins --> Configure System:
        server = Artifactory.server 'JFrogChina-Server'

        // Create a local build-info instance:
        buildInfo = Artifactory.newBuildInfo()
        buildInfo.project = 'slash-jenkins-conan-install'

        // Create a conan client instance:
        conanClient = Artifactory.newConanClient()

        conanClient.run(command: "config set general.revisions_enabled=True")

        // Add a new repository named 'conan-local' to the conan client.
        // The 'remote.add' method returns a 'serverName' string, which is used later in the script:
        resolveRepo = conanClient.remote.add server: server, repo: "slash-conan-remote"
        deployRepo = conanClient.remote.add server: server, repo: "slash-conan-dev-local"
    }

    stage("Conan build"){
        dir("conan_install"){
            // Run a conan build. The 'buildInfo' instance is passed as an argument to the 'run' method:
            conanClient.run(command: "install . --build missing -r ${resolveRepo}", buildInfo: buildInfo)
        }
    }

    stage("PublishBuildInfo"){
        // Create an upload command. The 'serverName' string is used as a conan 'remote', so that
        // the artifacts are uploaded into it:
        String command = "upload *  -r ${deployRepo} --confirm"

        // Run the upload command, with the same build-info instance as an argument:
        conanClient.run(command: command, buildInfo: buildInfo)

         // Publish the build-info to Artifactory:
        server.publishBuildInfo buildInfo
    }
        
}