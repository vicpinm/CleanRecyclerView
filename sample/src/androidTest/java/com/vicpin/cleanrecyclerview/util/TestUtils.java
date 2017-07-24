package com.vicpin.cleanrecyclerview.util;

import android.support.annotation.IdRes;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.util.HumanReadables;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.vicpin.cleanrecyclerview.sample.R;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by Victor on 09/02/16.
 */
public class TestUtils {

    public static final ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

    private static final class ActionOnItemViewAtPositionViewAction<VH extends RecyclerView
            .ViewHolder>
            implements

            ViewAction {
        private final int position;
        private final ViewAction viewAction;
        private final int viewId;

        private ActionOnItemViewAtPositionViewAction(int position,
                                                     @IdRes int viewId,
                                                     ViewAction viewAction) {
            this.position = position;
            this.viewAction = viewAction;
            this.viewId = viewId;
        }

        public Matcher<View> getConstraints() {
            return Matchers.allOf(new Matcher[]{
                    ViewMatchers.isAssignableFrom(RecyclerView.class), ViewMatchers.isDisplayed()
            });
        }

        public String getDescription() {
            return "actionOnItemAtPosition performing ViewAction: "
                    + this.viewAction.getDescription()
                    + " on item at position: "
                    + this.position;
        }

        public void perform(UiController uiController, View view) {
            RecyclerView recyclerView = (RecyclerView) view;
            (new ScrollToPositionViewAction(this.position)).perform(uiController, view);
            uiController.loopMainThreadUntilIdle();

            View targetView = recyclerView.getChildAt(this.position < recyclerView.getChildCount() ? this.position : recyclerView.getChildCount() - 1).findViewById(this.viewId);

            if (targetView == null) {
                throw (new PerformException.Builder()).withActionDescription(this.toString())
                        .withViewDescription(

                                HumanReadables.describe(view))
                        .withCause(new IllegalStateException(
                                "No view with id "
                                        + this.viewId
                                        + " found at position: "
                                        + this.position))
                        .build();
            } else {
                this.viewAction.perform(uiController, targetView);
            }
        }
    }

    private static final class ScrollToPositionViewAction implements ViewAction {
        private final int position;

        private ScrollToPositionViewAction(int position) {
            this.position = position;
        }

        public Matcher<View> getConstraints() {
            return Matchers.allOf(new Matcher[]{
                    ViewMatchers.isAssignableFrom(RecyclerView.class), ViewMatchers.isDisplayed()
            });
        }

        public String getDescription() {
            return "scroll RecyclerView to position: " + this.position;
        }

        public void perform(UiController uiController, View view) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.scrollToPosition(this.position);
        }
    }

    private static final class AssertionOnItemViewAtPosition<VH extends RecyclerView
            .ViewHolder>
            implements

            ViewAssertion {
        private final int position;
        private final ViewAssertion viewAssertion;
        private final int viewId;

        private AssertionOnItemViewAtPosition(int position,
                                                     @IdRes int viewId,
                                                     ViewAssertion assertion) {
            this.position = position;
            this.viewAssertion = assertion;
            this.viewId = viewId;
        }

        public Matcher<View> getConstraints() {
            return Matchers.allOf(new Matcher[]{
                    ViewMatchers.isAssignableFrom(RecyclerView.class), ViewMatchers.isDisplayed()
            });
        }

        @Override public void check(View view, NoMatchingViewException noViewFoundException) {
            RecyclerView recyclerView = (RecyclerView) view;
            scrollToPosition(this.position);

            View targetView = recyclerView.getChildAt(this.position < recyclerView.getChildCount() ? this.position : recyclerView.getChildCount() - 1).findViewById(this.viewId);

            if (targetView == null) {
                throw (new PerformException.Builder()).withActionDescription(this.toString())
                        .withViewDescription(

                                HumanReadables.describe(view))
                        .withCause(new IllegalStateException(
                                "No view with id "
                                        + this.viewId
                                        + " found at position: "
                                        + this.position))
                        .build();
            } else {
                this.viewAssertion.check(targetView, noViewFoundException);
            }
        }
    }

    //***********************************************************************************************************************
    // Auxiliary methods
    //***********************************************************************************************************************

    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    public static <VH extends RecyclerView.ViewHolder> ViewAction scrollToPosition(final int position) {
        return new ScrollToPositionViewAction(position);
    }

    public static <VH extends RecyclerView.ViewHolder> ViewAction actionOnItemViewAtPosition(int position, @IdRes int viewId, ViewAction viewAction) {
        return new ActionOnItemViewAtPositionViewAction(position, viewId, viewAction);
    }

    public static <VH extends RecyclerView.ViewHolder> ViewAssertion assertionOnItemViewAtPosition(int position, @IdRes int viewId, ViewAssertion assertion) {
        return new AssertionOnItemViewAtPosition(position, viewId, assertion);
    }

    //***********************************************************************************************************************
    // Utility methods to perform multiple actions or assertions over AdapterView and RecyclerView
    //***********************************************************************************************************************

    public static void doActionOverAdapterView(Class itemClass, int count, ViewAction action, int viewId) {

        for (int i = 0; i < count; i++) {
            onData(allOf(is(instanceOf(itemClass)))).atPosition(i)
                    .onChildView(withId(viewId)).perform(action);
        }

    }

    public static void doAssertionOverAdapterView(Class itemClass, int count, ViewAssertion assertion, int viewId) {

        for (int i = 0; i < count; i++) {
            onData(allOf(is(instanceOf(itemClass)))).atPosition(i)
                    .onChildView(withId(viewId)).check(assertion);
        }

    }

    public static void doActionOverRecyclerView(int count, ViewAction action, int viewId) {

        for (int i = 0; i < count; i++) {
            onView(withId(R.id.recyclerListView)).perform(
                    RecyclerViewActions.actionOnItemAtPosition(i, clickChildViewWithId(viewId)));
        }
    }

    public static void doAssertionOverRecyclerView(int count, ViewAssertion assertion, int viewId) {

        for (int i = 0; i < count; i++) {
            onView(withId(R.id.recyclerListView)).perform(
                    RecyclerViewActions.scrollToPosition(i));
            onView(withRecyclerView(R.id.recyclerListView).atPositionOnView(i, viewId)).check(assertion);

        }
    }

    public static void doActionOnRecyclerViewItem(int position, ViewAction action, int viewId) {
        onView(withId(R.id.recyclerListView)).perform(actionOnItemViewAtPosition(position, viewId, action));
    }

    public static void scrollListToPosition(int position){
        onView(withId(R.id.recyclerListView)).perform(
                RecyclerViewActions.scrollToPosition(position));
    }

}