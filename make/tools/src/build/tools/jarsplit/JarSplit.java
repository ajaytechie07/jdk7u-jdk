/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package build.tools.jarsplit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.util.Vector;
import java.lang.reflect.Array;

/*
 * This class is a utility class to take a file list and split them into
 * class file list and non-class file (such as image, audio file) list.
 */
public class JarSplit {
    /* classlist[0] contains class file list
     * classlist[1] contains non-class file list
     */
    private static Vector<String>[] classlist = (Vector<String>[])(new Vector[2]);

    /* The 2 class list name passed as arguments. */
    private static String[] fileNamelist = new String[2];

    private static void printUsage() {
        String help =
            "Usage:  java JarSplit classlist -o <class file list> <non-class file list> \n"
            + " This class takes a class list generated by JarReorder or some other tools \n"
            + "  and outputs two files which contain class file list and non-class file list.\n"
            + " Example: java JarSplit classlist -o classlist nonClasslist \n";

        System.err.println(help);
        System.exit(1);
    }

    public static void main(String[] args) {

        int arglen = args.length;

        if (arglen < 4 || !args[1].equals("-o")) {
            /* Print out the usage of this class. */
            printUsage();
        }

        for (int i = 0; i < 2; i++) {
            fileNamelist[i] = args[i + 2];
            classlist[i] = new Vector<String>();
        }

        if (generateClassLists(args[0]) == true) {
            writeClassListToOutput();
        }
    }

    /* This method takes the classlist and generate two lists.
       @param inFileName  the name of the file containing a list
       of the class files and non-class files.
       @exception FileNotFoundException if the passed in file is not found
                  IOException if there is any IO error
    */
    private static boolean generateClassLists(String inFileName) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(inFileName));
            boolean match = false;

            while (true) {
                String line = null;
                line = br.readLine();

                if (line == null) {
                    break;
                }

                /* Skip empty or comment lines. */
                if (line.length() == 0    ||
                    line.charAt(0) == '#') {
                    continue;
                }

                line = line.trim();

                if (!line.endsWith(".class")) {
                    classlist[1].add(line); /* Add to class list. */
                } else {
                    classlist[0].add(line); /* Add to non-class list. */
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.err.println("Can't find file \"" + inFileName + "\".");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        return true;
    }

    /* Helper method to write class list to the output file specified in the
       command.
    */
    private static void writeClassListToOutput() {

        try {
            for (int i = 0; i < fileNamelist.length; i++) {
                PrintStream out = new PrintStream(new FileOutputStream(fileNamelist[i]));

                for (int j = 0; j < classlist[i].size(); j++) {
                    out.println(classlist[i].elementAt(j));
                }

                out.flush();
                out.close();
            }
        } catch ( FileNotFoundException e ) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(3);
        }
    }
}
