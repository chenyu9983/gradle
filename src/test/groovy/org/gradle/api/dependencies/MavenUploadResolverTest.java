/*
 * Copyright 2007-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.dependencies;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.dependencies.ivy2Maven.DeployTaskFactory;
import org.gradle.BuildResult;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Expectations;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.artifact.ant.DeployTask;
import org.apache.maven.artifact.ant.Pom;
import org.apache.tools.ant.Project;
import org.hamcrest.Matcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.io.File;
import java.io.IOException;

/**
 * @author Hans Dockter
 */
@RunWith(org.jmock.integration.junit4.JMock.class)
public class MavenUploadResolverTest {
    private MavenUploadResolver mavenUploadResolver;

    private DeployTaskFactory deployTaskFactoryMock;
    private DeployTask deployTaskMock;

    private RemoteRepository testRepository;
    private RemoteRepository testSnapshotRepository;

    private JUnit4Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private static final File TEST_JAR_FILE = new File("somejar.jar");
    private static final File TEST_POM_FILE = new File("somepom.xml");

    @Before
    public void setUp() {
        deployTaskFactoryMock = context.mock(DeployTaskFactory.class);
        deployTaskMock = context.mock(DeployTask.class);
        context.checking(new Expectations() {{
            allowing(deployTaskFactoryMock).createDeployTask(); will(returnValue(deployTaskMock));
        }});
        mavenUploadResolver = new MavenUploadResolver();
        testRepository = new RemoteRepository();
        testSnapshotRepository = new RemoteRepository();
        mavenUploadResolver.setDeployTaskFactory(deployTaskFactoryMock);
        mavenUploadResolver.addRemoteRepository(testRepository);
        mavenUploadResolver.addRemoteRepository(testSnapshotRepository);
    }


    @Test(expected = InvalidUserDataException.class)
    public void publishSrcNull() throws IOException {
        mavenUploadResolver.publish(
                DefaultArtifact.newIvyArtifact(ModuleRevisionId.newInstance("org", "name", "1.0"), null),
                null,
                true);
    }

    @Test(expected = InvalidUserDataException.class)
    public void publishArtifactNull() throws IOException {
        mavenUploadResolver.publish(
                null,
                new File("somefile"),
                true);
    }

    @Test(expected = InvalidUserDataException.class)
    public void moreThanOnePomFile() throws IOException {
        publishTestPom();
        publishTestPom();
    }

    @Test(expected = InvalidUserDataException.class)
    public void moreThanOneArtifact() throws IOException {
        publishTestArtifact();
        publishTestArtifact();
    }

    @Test(expected = InvalidUserDataException.class)
    public void noPomFile() throws IOException {
        publishTestArtifact();
        mavenUploadResolver.commitPublishTransaction();
    }

    @Test(expected = InvalidUserDataException.class)
    public void noArtifactFile() throws IOException {
        publishTestPom();
        mavenUploadResolver.commitPublishTransaction();
    }

    @Test
    public void validDeployWithNoFilters() throws IOException {
        publishTestPom();
        publishTestArtifact();
        checkTransaction();
    }

    @Test
    public void validDeployWithFilters() throws IOException {
        publishTestPom();
        publishTestArtifact();
        final File testSrcFile = new File("anotherFile");
        mavenUploadResolver.setPublishFilter(new PublishFilter() {
            public boolean accept(Artifact artifact, File src, boolean overwrite) {
                return !src.equals(testSrcFile);
            }
        });
        mavenUploadResolver.publish(DefaultArtifact.newIvyArtifact(ModuleRevisionId.newInstance("org", "name", "1.0"), null),
                testSrcFile, true);
        checkTransaction();
    }

    private void checkTransaction() throws IOException {
        context.checking(new Expectations() {{
            one(deployTaskMock).addRemoteRepository(testRepository);
            one(deployTaskMock).addRemoteRepository(testSnapshotRepository);
            one(deployTaskMock).setFile(TEST_JAR_FILE);
            one(deployTaskMock).addPom(with(pomMatcher(TEST_POM_FILE)));
            one(deployTaskMock).setProject(with(any(Project.class)));
            one(deployTaskMock).execute();
        }});
        mavenUploadResolver.commitPublishTransaction();
    }

    private void publishTestPom() throws IOException {
        Artifact pomArtifact = DefaultArtifact.newIvyArtifact(ModuleRevisionId.newInstance("org", "name", "1.0"), null);
        mavenUploadResolver.publish(pomArtifact, TEST_POM_FILE, true);
    }

    private void publishTestArtifact() throws IOException {
        Artifact jarArtifact = new DefaultArtifact(ModuleRevisionId.newInstance("org", "name", "1.0"), null, "name", "jar", "jar");
        mavenUploadResolver.publish(jarArtifact, TEST_JAR_FILE, true);

    }

    private Matcher<Pom> pomMatcher(final File expectedPomFile) {
        return new BaseMatcher<Pom>() {
            public void describeTo(Description description) {
                description.appendText("matching pom");
            }

            public boolean matches(Object actual) {
                Pom actualPom = (Pom) actual;
                return actualPom.getFile().equals(expectedPomFile);
            }
        };
    }

}
