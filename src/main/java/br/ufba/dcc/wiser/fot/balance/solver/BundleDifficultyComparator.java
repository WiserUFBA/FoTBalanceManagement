/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.balance.solver;

import br.ufba.dcc.wiser.fot.balance.entity.Bundles;
import java.io.Serializable;
import java.util.Comparator;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 *
 * @author jeferson
 */
public class BundleDifficultyComparator implements Comparator<Bundles>, Serializable{

    @Override
    public int compare(Bundles o1, Bundles o2) {
        return new CompareToBuilder()
                .append(o1.getBundleCost(), o2.getBundleCost())
                .append(o1.getBundleMvnArtifact(), o2.getBundleMvnArtifact())
                .toComparison();
    }
    
}
