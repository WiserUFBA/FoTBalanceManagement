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

/**
 *
 * Log and other class utilities.
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class FoTBalanceUtils {
    
    /* Counter for balance operations check */
    public static int BALANCE_COUNTER = 0;
    
    /* System Messages line begin */
    public static final String WARNING_MSG_START = "[WARNING] ";
    public static final String ERROR_MSG_START = "[ERROR] ";
    public static final String INFO_MSG_START = "[INFO] ";
    
    /**
     * 
     * Print a warning message formatted with warning style.
     * 
     * @param warning_msg Message to be printed.
     */
    public static void warningMsg(String warning_msg){
        // TODO Store this in a log file too
        System.err.println(WARNING_MSG_START + warning_msg);
    }
    
    /**
     * 
     * Print a error message formatted with error style.
     * 
     * @param error_msg Message to be printed.
     */
    public static void errorMsg(String error_msg){
        // TODO Store this in a log file too
        System.err.println(ERROR_MSG_START + error_msg);
    }
    
    /**
     * 
     * Print a info message formatted with info style.
     * 
     * @param info_msg Message to be printed.
     */
    public static void infoMsg(String info_msg){
        // TODO Store this in a log file too
        System.err.println(INFO_MSG_START + info_msg);
    }
}
