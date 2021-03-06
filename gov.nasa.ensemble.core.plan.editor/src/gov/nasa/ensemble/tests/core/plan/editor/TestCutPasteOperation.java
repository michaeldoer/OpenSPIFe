/*******************************************************************************
 * Copyright 2014 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package gov.nasa.ensemble.tests.core.plan.editor;

import gov.nasa.ensemble.common.ui.IStructureModifier;
import gov.nasa.ensemble.common.ui.transfers.ITransferable;
import gov.nasa.ensemble.core.model.plan.EActivity;
import gov.nasa.ensemble.core.model.plan.EActivityGroup;
import gov.nasa.ensemble.core.model.plan.EPlan;
import gov.nasa.ensemble.core.model.plan.EPlanChild;
import gov.nasa.ensemble.core.model.plan.EPlanElement;
import gov.nasa.ensemble.core.model.plan.translator.WrapperUtils;
import gov.nasa.ensemble.core.model.plan.util.PlanElementXMLUtils;
import gov.nasa.ensemble.core.plan.PlanUtils;
import gov.nasa.ensemble.core.plan.editor.PlanClipboardCutOperation;
import gov.nasa.ensemble.core.plan.editor.PlanClipboardPasteOperation;
import gov.nasa.ensemble.core.plan.editor.PlanStructureModifier;
import gov.nasa.ensemble.emf.transaction.TransactionUtils;
import gov.nasa.ensemble.tests.core.plan.OperationTestPlanRecord;
import gov.nasa.ensemble.tests.core.plan.UndoableOperationTestCase;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Andrew
 *
 */
public class TestCutPasteOperation extends UndoableOperationTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PlanElementXMLUtils.setUpMap();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		PlanElementXMLUtils.tearDownMap();
	}
	
	public void testCutPasteActivitiesToActivity() {
		final OperationTestPlanRecord plan = new OperationTestPlanRecord();
		final EPlanElement[] selectedElements = new EActivity[] { 
				plan.activity1_2, plan.activity2_1, plan.activity3_3
				};
		final EPlanElement[] targetElements = new EActivity[] {
				plan.activity3_1
				};
		Runnable assertPostconditions = new Runnable() {
			public void run() {
				EPlanElement target = targetElements[targetElements.length - 1];
				EActivity activity = ((EActivity)target);
				int pos = activity.getListPosition() + 1;
				List<? extends EPlanChild> siblings = activity.getParent().getChildren();
				for (int i = 0 ; i < selectedElements.length ; i++) {
					EPlanElement addedElement = siblings.get(pos + i);
					EPlanElement expectedElement = selectedElements[i];
					assertEquals(expectedElement, addedElement);
				}
			}
		};
		cutPasteElements(plan, selectedElements, targetElements, assertPostconditions);
		WrapperUtils.dispose(plan.plan);
	}

	public void testCutPasteActivitiesToGroup2() {
		final OperationTestPlanRecord plan = new OperationTestPlanRecord();
		final EPlanElement[] selectedElements = new EActivity[] { 
				plan.activity1_2, plan.activity2_1, plan.activity3_3
				};
		final EPlanElement[] targetElements = new EActivityGroup[] {
				plan.group2
				};
		Runnable assertPostconditions = new Runnable() {
			public void run() {
				EPlanElement target = targetElements[targetElements.length - 1];
				EActivityGroup group = ((EActivityGroup)target);
				int pos = group.getChildren().size() - selectedElements.length;
				List<? extends EPlanChild> siblings = group.getChildren();
				for (int i = 0 ; i < selectedElements.length ; i++) {
					EPlanElement addedElement = siblings.get(pos + i);
					EPlanElement expectedElement = selectedElements[i];
					assertEquals(expectedElement, addedElement);
				}
			}
		};
		cutPasteElements(plan, selectedElements, targetElements, assertPostconditions);
		WrapperUtils.dispose(plan.plan);
	}

	public void testCutPasteGroupsToGroup() {
		final OperationTestPlanRecord plan = new OperationTestPlanRecord();
		final EPlanElement[] selectedElements = new EActivityGroup[] { 
				plan.group1, plan.group3
				};
		final EPlanElement[] targetElements = new EActivityGroup[] {
				plan.group2
				};
		final int childCount = plan.group2.getChildren().size();
		Runnable assertPostconditions = new Runnable() {
			public void run() {
				EPlanElement target = targetElements[targetElements.length - 1];
				EActivityGroup group = ((EActivityGroup)target);
				int pos;
				List<? extends EPlanChild> siblings;
				if (PlanUtils.ALLOW_GENERALIZED_HEIRARCHY_NESTING_EDITS) {
					pos = childCount;
					siblings = group.getChildren();
				} else {
					pos = group.getListPosition() + 1;
					siblings = group.getParent().getChildren();
				}
				for (int i = 0 ; i < selectedElements.length ; i++) {
					EPlanElement addedElement = siblings.get(pos + i);
					EPlanElement expectedElement = selectedElements[i];
					assertEquals(expectedElement, addedElement);
				}
			}
		};
		cutPasteElements(plan, selectedElements, targetElements, assertPostconditions);
		WrapperUtils.dispose(plan.plan);
	}

	public void testCutPasteGroupsToPlan() {
		final OperationTestPlanRecord plan = new OperationTestPlanRecord();
		final EPlanElement[] selectedElements = new EActivityGroup[] { 
				plan.group1, plan.group3
				};
		final EPlanElement[] targetElements = new EPlan[] {
				plan.plan
				};
		Runnable assertPostconditions = new Runnable() {
			public void run() {
				EPlanElement target = targetElements[targetElements.length - 1];
				EPlan plan = ((EPlan)target);
				int pos = plan.getChildren().size() - selectedElements.length; 
				List<? extends EPlanChild> siblings = plan.getChildren();
				for (int i = 0 ; i < selectedElements.length ; i++) {
					EPlanElement addedElement = siblings.get(pos + i);
					EPlanElement expectedElement = selectedElements[i];
					assertEquals(expectedElement, addedElement);
				}
			}
		};
		cutPasteElements(plan, selectedElements, targetElements, assertPostconditions);
		WrapperUtils.dispose(plan.plan);
	}

	/**
	 * @param plan
	 * @param selectedElements
	 * @param targetElements
	 * @param assertPostconditions
	 */
	private void cutPasteElements(final OperationTestPlanRecord plan, final EPlanElement[] selectedElements, final EPlanElement[] targetElements, Runnable assertPostconditions) {
		final IStructuredSelection selection = new StructuredSelection(selectedElements);
		final IStructureModifier modifier = PlanStructureModifier.INSTANCE;
		ITransferable transferable = modifier.getTransferable(selection);
		IUndoableOperation cut = new PlanClipboardCutOperation(transferable, modifier);
		cut.addContext(TransactionUtils.getUndoContext(plan.plan));
		// cut
		try {
			IOperationHistory history = OperationHistoryFactory.getOperationHistory();
			IStatus result = history.execute(cut, null, null);
			assertTrue("failed to execute: " + cut.getLabel(), result.isOK());
		} catch (ExecutionException ee) {
			fail("failed to execute");
		}

		final IStructuredSelection targetSelection = new StructuredSelection(targetElements);
		IUndoableOperation paste = new PlanClipboardPasteOperation(targetSelection, modifier);
		testUndoableOperation(plan.plan, paste, assertPostconditions);
	}

}
