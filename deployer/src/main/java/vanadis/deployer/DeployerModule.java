/*
 * Copyright 2009 Kjetil Valstadsve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vanadis.deployer;

import vanadis.common.io.Files;
import vanadis.core.lang.ToString;
import vanadis.common.time.TimeSpan;
import vanadis.common.text.Printer;
import vanadis.ext.*;
import vanadis.osgi.Context;

import java.io.File;

@Module(moduleType = "deployer", launch = @AutoLaunch(name = "deployer"))
public final class DeployerModule extends AbstractModule implements ContextAware {

    private static final TimeSpan PAUSE = TimeSpan.HALF_MINUTE;

    static final String COMMAND = "v-deploy";

    private ActiveDeployer activeDeployer;

    @Override
    public void activate() {
        File home = new File(context().getHome());
        this.activeDeployer = createDeployer(context(), home);
        new Thread(activeDeployer, "Deploy").start();
    }

    @Expose
    public Command getDeployCommand() {
        return new GenericCommand(COMMAND, "run deploy cycle", context(), new TriggerCycle());
    }

    @Override
    public void closed() {
        if (activeDeployer != null) {
            activeDeployer.stop();
        }
    }

    private static ActiveDeployer createDeployer(Context context, File home) {
        return new ActiveDeployer(new DeployImpl(context), PAUSE,
                                  new DirectoryBundleExplorer(bundleDir(home), tmpDir(home), ".zip", ".jar"),
                                  new DirectoryLaunchExplorer(serviceDir(home), ".xml"));
    }

    private static File tmpDir(File home) {
        return Files.getDirectory(home, "var", "tmp");
    }

    private static File bundleDir(File home) {
        return Files.getDirectory(home, "deploy", "bundle");
    }

    private static File serviceDir(File home) {
        return Files.getDirectory(home, "deploy", "service");
    }

    @Override
    public String toString() {
        return ToString.of(this, activeDeployer);
    }

    private class TriggerCycle implements CommandExecution {
        @Override
        public void exec(String command, String[] args, Printer ps, Context context) {
            activeDeployer.triggerCycle();
        }
    }
}
