/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2018 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

"use strict";

const Tools = require("./tools.js");
const tools = new Tools();

let gitTag = process.env.CIRCLE_TAG;
if (!gitTag) {
    throw "Cannot release without a valid git tag";
}
let targetVersion = gitTag.match(/^@release\-?(\d+\.\d+.\d+)?$/);
if (!targetVersion) {
    throw "Cannot release without a valid release version";
}

targetVersion = targetVersion[1];

tools.gitImpersonate('CircleCi', 'noreply@circleci.com', () => {
    try {
        tools.stage("RELEASE");

        // We cannot find out what git branch has the tag, so we assume/enforce that releases are done on master
        console.log("Checking out the master branch so we can commit and push");
        tools.sh("git checkout master");

        // Prepare artifact signing
        tools.prepareGPGKey();

        // Create release tag
        tools.sh("mvn tycho-versions:set-version -DnewVersion=" + targetVersion);
        tools.sh("git commit -am 'Set version to " + targetVersion + " in preparation for release");
        tools.sh("git tag -a -m 'Tagged " + targetVersion + "' aem-eclipse-developer-tools-" + targetVersion);

        // Push release tag
        tools.sh("git push && git push --tags");

        // Prepare for next development cycle
        let newVersion = targetVersion.substr(0, targetVersion.length - 1) +
            (Number(targetVersion.substr(targetVersion.length - 1)) + 1).toString() +
            "-SNAPSHOT";
        tools.sh("mvn tycho-versions:set-version -DnewVersion=" + newVersion);
        tools.sh("git commit -am 'Set version to " + newVersion + " after release' && git push");

        tools.stage("RELEASE DONE");

        // TODO: Deploy update site
    } finally {
        tools.removeGitTag(gitTag);
        tools.removeGPGKey()
    }
});
