 node("conan_node") {
        // Clone the code from github:
        git url :'git@github.com:slash-l/app-conan.git' , branch : "main"

        // Obtain an Artifactory server instance, defined in Jenkins --> Manage Jenkins --> Configure System:
        def server = Artifactory.server 'JFrogChina-Server'

        // Create a local build-info instance:
        def buildInfo = Artifactory.newBuildInfo()
        buildInfo.project = 'slash-jenkins-conan-install'

        // Create a conan client instance:
        def conanClient = Artifactory.newConanClient()

        // Add a new repository named 'conan-local' to the conan client.
        // The 'remote.add' method returns a 'serverName' string, which is used later in the script:
        String serverName = conanClient.remote.add server: server, repo: "slash-conan-dev-local"
        
        // Run a conan build. The 'buildInfo' instance is passed as an argument to the 'run' method:
        conanClient.run(command: "install ./conan_install/ --build missing", buildInfo: buildInfo)

        // Create an upload command. The 'serverName' string is used as a conan 'remote', so that
        // the artifacts are uploaded into it:
        String command = "upload *  -r ${serverName} --confirm"

        // Run the upload command, with the same build-info instance as an argument:
        conanClient.run(command: command, buildInfo: buildInfo)

         // Publish the build-info to Artifactory:
        server.publishBuildInfo buildInfo
}