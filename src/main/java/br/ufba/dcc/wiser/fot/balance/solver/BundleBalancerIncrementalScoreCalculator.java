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
package br.ufba.dcc.wiser.fot.balance.solver;

import br.ufba.dcc.wiser.fot.balance.entity.Host;
import br.ufba.dcc.wiser.fot.balance.entity.Group;
import br.ufba.dcc.wiser.fot.balance.entity.Bundles;
import br.ufba.dcc.wiser.fot.balance.utils.FoTBalanceUtils;
import java.util.HashMap;
import java.util.Map;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.incremental.AbstractIncrementalScoreCalculator;

/**
 *
 * @author jeferson
 */
public class BundleBalancerIncrementalScoreCalculator extends AbstractIncrementalScoreCalculator<Group> {

    private Map<Host, Integer> capacityUsedMap;
    private Map<Host, Integer> bundleCountMap;
    
    private int hardScore;
    private int softScore;

    private void insert(Bundles bundle) {
        /* Imprime o resultado da rodada */
        FoTBalanceUtils.BALANCE_COUNTER++;
        System.out.println( "Rodada " + FoTBalanceUtils.BALANCE_COUNTER + " => "
                + "Hard Score = " + hardScore + " | Soft Score = " + softScore);
        
        Host host = bundle.getHostAssociated();
        if (host != null) {
            int capacity = host.getHostCapacity();
            int oldCapacityUsed = capacityUsedMap.get(host);
            int oldCapacityAvailable = capacity - oldCapacityUsed;
            int newCapacityUsed = oldCapacityUsed + bundle.getBundleCost();
            int newCapacityAvailable = capacity - newCapacityUsed;
            hardScore += Math.min(newCapacityAvailable, 0) - Math.min(oldCapacityAvailable, 0);
            capacityUsedMap.put(host, newCapacityUsed);
            
            int oldBundleCount = bundleCountMap.get(host);
            if(oldBundleCount == 0){
                softScore += 1;
            }
            int newBundleCount = oldBundleCount + 1;
            bundleCountMap.put(host, newBundleCount);
        }
    }
    
    private void retract(Bundles bundle) {
        /* Imprime o resultado da rodada */
        FoTBalanceUtils.BALANCE_COUNTER++;
        System.out.println( "Rodada " + FoTBalanceUtils.BALANCE_COUNTER + " => "
                + "Hard Score = " + hardScore + " | Soft Score = " + softScore);
        
        Host host = bundle.getHostAssociated();
        if (host != null) {
            int capacity = host.getHostCapacity();
            int oldCapacityUsed = capacityUsedMap.get(host);
            int oldCapacityAvailable = capacity - oldCapacityUsed;
            int newCapacityUsed = oldCapacityUsed - bundle.getBundleCost();
            int newCapacityAvailable = capacity - newCapacityUsed;
            hardScore += Math.min(newCapacityAvailable, 0) - Math.min(oldCapacityAvailable, 0);
            capacityUsedMap.put(host, newCapacityUsed);

            int oldBundleCount = bundleCountMap.get(host);
            int newBundleCount = oldBundleCount - 1;
            if(newBundleCount == 0){
                softScore -= 1;
            }
            bundleCountMap.put(host, newBundleCount);
        }
    }
    
    @Override
    public void resetWorkingSolution(Group workingSolution) {
        /* Inicializa a estrutura de balanceamento */
        System.out.println("\n#Reinicializando a estrutura de Balanceamento Incremental\n");
        
        /* Reset counter of balance operations */
        FoTBalanceUtils.BALANCE_COUNTER = 0;
        
        int numberOfHosts = workingSolution.getHostList().size();
        capacityUsedMap = new HashMap<>(numberOfHosts);
        bundleCountMap = new HashMap<>(numberOfHosts);
        
        hardScore = 0;
        softScore = -numberOfHosts;
        
        for (Host host : workingSolution.getHostList()) {
            capacityUsedMap.put(host, 0);
            bundleCountMap.put(host, 0);
        }
        
        for (Bundles bundle : workingSolution.getBundleList()) {
            insert(bundle);
        }
    }

    @Override
    public void beforeEntityAdded(Object entity) {
        /* Do nothing */        
    }

    @Override
    public void afterEntityAdded(Object entity) {
        insert((Bundles) entity);
    }

    @Override
    public void beforeVariableChanged(Object entity, String variableName) {
        retract((Bundles) entity);
    }

    @Override
    public void afterVariableChanged(Object entity, String variableName) {
        insert((Bundles) entity);
    }

    @Override
    public void beforeEntityRemoved(Object entity) {
        retract((Bundles) entity);
    }

    @Override
    public void afterEntityRemoved(Object entity) {
        /* Do nothing */
    }

    @Override
    public Score calculateScore() {
        return HardSoftScore.valueOf(hardScore, softScore);
    }
   
}