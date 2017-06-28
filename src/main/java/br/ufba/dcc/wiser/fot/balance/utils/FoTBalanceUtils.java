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
package br.ufba.dcc.wiser.fot.balance.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufba.dcc.wiser.fot.balance.Activator;

/**
 *
 * Log and other class utilities.
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class FoTBalanceUtils {
    
    /* Counter for balance operations check */
    public static int BALANCE_COUNTER = 0;
    
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    
    /**
     * 
     * Print a warning message formatted with warning style.
     * 
     * @param warning_msg Message to be printed.
     */
    public static void warn(String warning_msg){
        // TODO Store this in a log file too
        LOG.warn(warning_msg);
    }
    
    /**
     * 
     * Print a error message stored in log file and that use SLF4J.
     * 
     * @param error_msg Message to be printed.
     */
    public static void error(String error_msg){
        // TODO Store this in a log file too
        LOG.error(error_msg);
    }
    
    /**
     * 
     * Print a info message stored in log file and that use SLF4J.
     * 
     * @param info_msg Message to be printed.
     */
    public static void info(String info_msg){
        // TODO Store this in a log file too
        LOG.info(info_msg);
    }
    
    /**
     * 
     * Print a debug message stored in log file and that use SLF4J.
     * 
     * @param debug_msg Message to be printed.
     */
    public static void debug(String debug_msg){
        // TODO Store this in a log file too
        LOG.debug(debug_msg);
    }
    
    /**
     * 
     * Print a debug message stored in log file and that use SLF4J.
     * 
     * @param trace_msg Message to be printed.
     */
    public static void trace(String trace_msg){
        // TODO Store this in a log file too
        LOG.trace(trace_msg);
    }
}
