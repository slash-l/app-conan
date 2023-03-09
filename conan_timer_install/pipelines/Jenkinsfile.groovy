 def server
 def buildInfo
 def conanClient
 
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

        // Create a conan client instance:
        conanClient = Artifactory.newConanClient()
        // conanClient = Artifactory.newConanClient userHome: "/Users/jingyil"

        conanClient.run(command: "config set general.revisions_enabled=True")
        
    }

    stage("Conan build"){
        dir("conan_timer_install"){
            // Add a new repository named 'conan-local' to the conan client.
            // The 'remote.add' method returns a 'serverName' string, which is used later in the script:
            // String resolveRepo = conanClient.remote.add server: server, repo: "slash-conan-remote"

            // Run a conan build. The 'buildInfo' instance is passed as an argument to the 'run' method:
            conanClient.run(command: "install . --build missing", buildInfo: buildInfo)
        }
    }

    stage("PublishBuildInfo"){
        String deployRepo = conanClient.remote.add server: server, repo: "slash-conan-dev-local"

        // Create an upload command. The 'deployRepo' string is used as a conan 'remote', so that
        // the artifacts are uploaded into it:
        String command = "upload *  -r ${deployRepo} --confirm"

        // Run the upload command, with the same build-info instance as an argument:
        conanClient.run(command: command, buildInfo: buildInfo)

        // Publish the build-info to Artifactory:
        server.publishBuildInfo buildInfo
    }
        
}