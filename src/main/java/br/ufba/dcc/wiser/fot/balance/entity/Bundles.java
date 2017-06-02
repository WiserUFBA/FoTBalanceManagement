/*
 * The MIT License
 *
 * Copyright 2017 Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.ufba.dcc.wiser.fot.balance.entity;

import br.ufba.dcc.wiser.fot.balance.Controller;
import br.ufba.dcc.wiser.fot.balance.exceptions.UnassociatedHostException;
import br.ufba.dcc.wiser.fot.balance.solver.BundleDifficultyComparator;
import br.ufba.dcc.wiser.fot.balance.solver.HostStrengthComparator;
import com.google.gson.annotations.SerializedName;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
@PlanningEntity(difficultyComparatorClass = BundleDifficultyComparator.class)
public class Bundles {

    /* Maven GroupID */
    @SerializedName("groupId")
    private String bundle_mvn_group;
    /* Maven ArtifactID */
    @SerializedName("artifactId")
    private String bundle_mvn_artifact;
    /* Maven Bundle Version */
    @SerializedName("artifactVersion")
    private String bundle_mvn_version;
    /* Bundle Group */
    private Group bundle_group;
    /* Bundle cost */
    @SerializedName("bundleCost")
    private int bundle_cost;
    /* Bundle Host Associated */
    private Host host_associated;

    /* Maven Format */
    public static String MVN_URL_FORMAT = "mvn=%s/%s/%s";

    /* Karaf Install URL */
    public static String KARAF_INSTALL_URL_FORMAT = "url:http://%s:%s/bundleInstall?%s";

    /* Karaf Uninstall URL */
    public static String KARAF_UNINSTALL_URL_FORMAT = "url:http://%s:%s/bundleUninstall?%s";
    
    /**
     *
     * Construct a bundle object reference.
     *
     * @param mvn_artifact Maven artifactId.
     * @param mvn_group Maven groupId.
     * @param mvn_version Maven version.
     * @param bundle_group Maven group object.
     * @param bundle_cost Cost of the given bundle.
     */
    public Bundles(String mvn_artifact, String mvn_group, String mvn_version,
            Group bundle_group, int bundle_cost) {
        /* Set object properties */
        this.bundle_mvn_artifact = mvn_artifact;
        this.bundle_mvn_group = mvn_group;
        this.bundle_mvn_version = mvn_version;
        this.bundle_group = bundle_group;
        this.bundle_cost = bundle_cost;
        this.host_associated = null;
    }

    /**
     *
     * Construct maven object reference without group object.
     *
     * @param mvn_artifact Maven artifactId.
     * @param mvn_group Maven groupId.
     * @param mvn_version Maven version.
     * @param bundle_cost Cost of the given bundle.
     */
    public Bundles(String mvn_artifact, String mvn_group, String mvn_version,
            int bundle_cost) {
        this(mvn_artifact, mvn_group, mvn_version, null, bundle_cost);
    }

    /**
     * 
     * Blank constructor for clone operations
     * 
     */
    public Bundles(){
        this("","","", 0);
    }
    
    /**
     *
     * Return maven installation URL.
     *
     * @return Maven string installation url.
     */
    public String getMavenURL() {
        return String.format(MVN_URL_FORMAT, bundle_mvn_group, 
                            bundle_mvn_artifact, bundle_mvn_version);
    }

    /**
     *
     * Get Karaf installation URL.
     *
     * @return Karaf string installation url.
     * @throws UnassociatedHostException Throw exception if there's no host
     * associated with this bundle.
     */
    public String getKarafInstallURL() throws UnassociatedHostException {
        if (host_associated == null) {
            throw new UnassociatedHostException();
        }
        return String.format(KARAF_INSTALL_URL_FORMAT, host_associated.getHostAddress(), 
                            Controller.KARAF_INSTALL_PORT, getMavenURL());
    }

    /**
     *
     * Get Karaf installation URL.
     *
     * @return Karaf string installation url.
     * @throws UnassociatedHostException Throw exception if there's no host
     * associated with this bundle.
     */
    public String getKarafUninstallURL() throws UnassociatedHostException {
        if (host_associated == null) {
            throw new UnassociatedHostException();
        }
        return String.format(KARAF_UNINSTALL_URL_FORMAT, host_associated.getHostAddress(), 
                            Controller.KARAF_INSTALL_PORT, getMavenURL());
    }
    
    /**
     *
     * Disassociate host from this bundle.
     *
     */
    public void disassociateHost() {
        host_associated = null;
    }

    /**
     *
     * Return host associated with this bundle.
     *
     * @return Host associated with this bundle.
     */
    @PlanningVariable(valueRangeProviderRefs = {"hostRange"},
        strengthComparatorClass = HostStrengthComparator.class)
    public Host getHostAssociated() {
        return host_associated;
    }
    
    /**
     *
     * Set host associated with this bundle.
     *
     * @param host_associated New host associated with this bundle.
     */
    public void setHostAssociated(Host host_associated) {
        this.host_associated = host_associated;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Basic Getter and Setter Functions">
    /**
     *
     * Get Bundle Maven ArtifactID.
     *
     * @return Bundle Maven ArtifactID.
     */
    public String getBundleMvnArtifact() {
        return bundle_mvn_artifact;
    }

    /**
     *
     * Set Bundle Maven ArtifactID.
     *
     * @param bundle_mvn_artifact Bundle maven artifact.
     */
    public void setBundleMvnArtifact(String bundle_mvn_artifact) {
        this.bundle_mvn_artifact = bundle_mvn_artifact;
    }

    /**
     *
     * Get Bundle Maven GroupID.
     *
     * @return Bundle maven GroupID.
     */
    public String getBundleMvnGroup() {
        return bundle_mvn_group;
    }

    /**
     *
     * Set Bundle maven GroupID.
     *
     * @param bundle_mvn_group Bundle maven group.
     */
    public void setBundleMvnGroup(String bundle_mvn_group) {
        this.bundle_mvn_group = bundle_mvn_group;
    }

    /**
     *
     * Get bundle maven version.
     *
     * @return Bundle version.
     */
    public String getBundleMvnVersion() {
        return bundle_mvn_version;
    }

    /**
     *
     * Set bundle maven version.
     *
     * @param bundle_mvn_version Version string.
     */
    public void setBundleMvnVersion(String bundle_mvn_version) {
        this.bundle_mvn_version = bundle_mvn_version;
    }

    /**
     *
     * Return the bundle group.
     *
     * @return Bundle group.
     */
    public Group getBundleGroup() {
        return bundle_group;
    }

    /**
     *
     * Set bundle group.
     *
     * @param bundle_group New bundle group.
     */
    public void setBundleGroup(Group bundle_group) {
        this.bundle_group = bundle_group;
    }

    /**
     *
     * Return the bundle cost.
     *
     * @return Bundle cost.
     */
    public int getBundleCost() {
        return bundle_cost;
    }

    /**
     *
     * Set bundle cost.
     *
     * @param bundle_cost New bundle cost.
     */
    public void setBundleGroup(int bundle_cost) {
        this.bundle_cost = bundle_cost;
    }

    // </editor-fold>
}
