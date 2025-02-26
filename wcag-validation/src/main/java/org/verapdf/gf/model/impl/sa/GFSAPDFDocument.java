/**
 * This file is part of veraPDF Validation, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Validation is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Validation as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Validation as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.gf.model.impl.sa;

import org.verapdf.model.GenericModelObject;
import org.verapdf.model.baselayer.Object;
import org.verapdf.model.salayer.SAPDFDocument;
import org.verapdf.model.salayer.SAStructTreeRoot;
import org.verapdf.gf.model.impl.containers.StaticStorages;
import org.verapdf.wcag.algorithms.semanticalgorithms.AccumulatedNodeSemanticChecker;
import org.verapdf.wcag.algorithms.semanticalgorithms.ContrastRatioChecker;

import java.util.*;

/**
 * @author Maxim Plushchov
 */
public class GFSAPDFDocument extends GenericModelObject implements SAPDFDocument {

    public static final String DOCUMENT_TYPE = "SAPDFDocument";

    protected org.verapdf.pd.PDDocument document;

    public static final int MAX_NUMBER_OF_ELEMENTS = 1;

    public static final String STRUCTURE_TREE_ROOT = "StructTreeRoot";

    public static final String PAGES = "pages";

    private List<GFSAPage> pages;

    private GFSAStructTreeRoot treeRoot = null;

    public GFSAPDFDocument(org.verapdf.pd.PDDocument document) {
        super(DOCUMENT_TYPE);
        this.document = document;
        StaticStorages.clearAllContainers();
        checkSemantic();
    }

    @Override
    public List<? extends Object> getLinkedObjects(String link) {
        switch (link) {
            case STRUCTURE_TREE_ROOT:
                return this.getStructureTreeRoot();
            case PAGES:
                return getPages();
            default:
                return super.getLinkedObjects(link);
        }
    }

    private List<GFSAPage> parsePages() {
        List<GFSAPage> result = new ArrayList<>();
        List<org.verapdf.pd.PDPage> rawPages = document.getPages();
        for (org.verapdf.pd.PDPage rawPage : rawPages) {
            result.add(new GFSAPage(rawPage));
        }
        return Collections.unmodifiableList(result);
    }

	private List<GFSAPage> getPages() {
        if (this.pages == null) {
            this.pages = parsePages();
        }
        return this.pages;
	}

    private void parseStructureTreeRoot() {
        org.verapdf.pd.structure.PDStructTreeRoot root = document.getStructTreeRoot();
        if (root != null) {
            this.treeRoot = new GFSAStructTreeRoot(root);
        }
    }

    private List<SAStructTreeRoot> getStructureTreeRoot() {
        if (treeRoot == null) {
            parseStructureTreeRoot();
        }
        if (treeRoot != null) {
            List<SAStructTreeRoot> res = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
            res.add(treeRoot);
            return Collections.unmodifiableList(res);
        }
        return Collections.emptyList();
    }

    private void parseChunks() {
        if (this.pages == null) {
            pages = parsePages();
        }
        for (GFSAPage page : pages) {
            GFSAContentStream contentStream = page.getContentStream();
            if (contentStream != null) {
                contentStream.parseChunks();
            }
        }
    }

    private void checkSemantic() {
        parseChunks();
        parseStructureTreeRoot();
        if (treeRoot != null) {
            AccumulatedNodeSemanticChecker accumulatedNodeSemanticChecker = new AccumulatedNodeSemanticChecker();
            accumulatedNodeSemanticChecker.checkSemanticTree(treeRoot);
            if (document.getDocument().getFileName() != null) {
                ContrastRatioChecker contrastRatioChecker = new ContrastRatioChecker();
                contrastRatioChecker.checkSemanticTree(treeRoot, document.getDocument().getFileName());
            }
        }
    }

}
