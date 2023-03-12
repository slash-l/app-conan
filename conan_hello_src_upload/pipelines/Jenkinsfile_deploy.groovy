node("master") {
    def server
    def buildInfo
    def conanClient

    stage("SCM"){
        // Clone the code from github:
        git url :'git@github.com:slash-l/app-conan.git' , branch : "main"
        sh "sed -i \"\" \"s#BUILD_NUMBER#${BUILD_NUMBER}#g\" ./conan_hello_src_upload/conanfile.py"
    }

    stage("Artifactory Configure"){
        // Obtain an Artifactory server instance, defined in Jenkins --> Manage Jenkins --> Configure System:
        server = Artifactory.server 'JFrogChina-Server'

        // Create a local build-info instance:
        buildInfo = Artifactory.newBuildInfo()

        // Create a conan client instance:
        conanClient = Artifactory.newConanClient()
        // conanClient = Artifactory.newConanClient userHome: "/Users/jingyil"

        // conanClient.run(command: "config set general.revisions_enabled=True")
        
    }

    stage("Conan build"){
        dir("conan_hello_src_upload"){
            // Add a new repository named 'conan-local' to the conan client.
            // The 'remote.add' method returns a 'serverName' string, which is used later in the script:
            // String resolveRepo = conanClient.remote.add server: server, repo: "slash-conan-remote"

            // Run a conan build. The 'buildInfo' instance is passed as an argument to the 'run' method:
            conanClient.run(command: "install . --build missing", buildInfo: buildInfo)
            conanClient.run(command: "create . user/testing", buildInfo: buildInfo)
        }
    }

    stage("PublishBuildInfo"){
        String deployRepo = conanClient.remote.add server: server, repo: "slash-conan-dev-local", force: true

        // Create an upload command. The 'deployRepo' string is used as a conan 'remote', so that
        // the artifacts are uploaded into it:
        String command = "upload hello/0.1.${BUILD_NUMBER}@user/testing -r ${deployRepo} --all --confirm"
        // String command = "upload hello/0.1@user/testing -r ${deployRepo} --all --confirm"

        // Run the upload command, with the same build-info instance as an argument:
        conanClient.run(command: command, buildInfo: buildInfo)

        // Publish the build-info to Artifactory:
        server.publishBuildInfo buildInfo
    }
        
}