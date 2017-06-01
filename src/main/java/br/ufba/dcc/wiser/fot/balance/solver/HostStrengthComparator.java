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

import java.io.Serializable;
import java.util.Comparator;
import br.ufba.dcc.wiser.fot.balance.entity.Host;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 *
 * Comparator of Hard constants of FoT Balance Management.
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class HostStrengthComparator implements Comparator<Host>, Serializable {

    @Override
    public int compare(Host a, Host b) {
        return new CompareToBuilder()
                .append(a.getHostCapacity(), b.getHostCapacity())
                .append(a.getHostID(), b.getHostID())
                .toComparison();
    }

}
