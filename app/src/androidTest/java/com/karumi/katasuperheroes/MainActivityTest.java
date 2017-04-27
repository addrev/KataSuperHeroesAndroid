/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karumi.katasuperheroes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers.Visibility;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.recyclerview.RecyclerViewInteraction;
import com.karumi.katasuperheroes.recyclerview.RecyclerViewInteraction.ItemViewAssertion;
import com.karumi.katasuperheroes.ui.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.R.id.list;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    public static final String EMTPY_CASE_STRING = "¯\\_(ツ)_/¯";
    @Rule
    public DaggerMockRule<MainComponent> daggerRule =
            new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
                    new DaggerMockRule.ComponentSetter<MainComponent>() {
                        @Override
                        public void setComponent(MainComponent component) {
                            SuperHeroesApplication app =
                                    (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule
    public IntentsTestRule<MainActivity> activityRule =
            new IntentsTestRule<>(MainActivity.class, true, false);

    @Mock
    SuperHeroesRepository repository;

    @Test
    public void showsEmptyCaseIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes();

        startActivity();

        onView(withText(EMTPY_CASE_STRING)).check(matches(isDisplayed()));
    }

    @Test
    public void doesntShowEmptyCaseIfThereAreSuperHeroes() throws Exception {
        givenThereAreSomeSuperHeroes(100);

        startActivity();

        onView(withText(EMTPY_CASE_STRING)).check(matches(not(isDisplayed())));
    }


    @Test
    public void doesNotShowProgressBarIfThereAreSomeSuperHeroes() throws Exception {
        givenThereAreSomeSuperHeroes(10);

        startActivity();

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showsSuperHeroNameIfThereIsOneSuperHero() throws Exception {
        List<SuperHero> heroesList = givenThereAreSomeSuperHeroes(1);

        startActivity();

        onView(withText(heroesList.get(0).getName())).check(matches(isDisplayed()));
    }

    @Test
    public void showsManySuperHeroesWhenThereAreManySuperheros() throws Exception {
        List<SuperHero> heroesList = givenThereAreSomeSuperHeroes(10);

        startActivity();

        RecyclerViewInteraction.<SuperHero>onRecyclerView((withId(R.id.recycler_view))).withItems(heroesList).check(new ItemViewAssertion<SuperHero>() {
            @Override
            public void check(SuperHero item, View view, NoMatchingViewException e) {
                matches(hasDescendant(withText(item.getName()))).check(view,e);
            }
        });
    }


    @Test
    public void showsAvengersBadge() throws Exception {
        List<SuperHero> heroesList = givenThereAreSomeSuperHeroes(1);

        startActivity();

        RecyclerViewInteraction.<SuperHero>onRecyclerView((withId(R.id.recycler_view))).withItems(heroesList).check(new ItemViewAssertion<SuperHero>() {
            @Override
            public void check(SuperHero item, View view, NoMatchingViewException e) {
                matches(hasDescendant(allOf(withId(R.id.iv_avengers_badge),withEffectiveVisibility(
                        item.isAvenger()?Visibility.VISIBLE:Visibility.GONE)))).check(view,e);
            }
        });
    }



    private void givenThereAreNoSuperHeroes() {
        when(repository.getAll()).thenReturn(Collections.<SuperHero>emptyList());
    }

    private List<SuperHero> givenThereAreSomeSuperHeroes(int number) {
        List<SuperHero> superHeroList = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            SuperHero hero = new SuperHero("Hero" + i, "http://www.photo.com/" + i, i % 2 == 0, "Hero" + i + " description");
            superHeroList.add(hero);
        }
        when(repository.getAll()).thenReturn(superHeroList);
        return superHeroList;
    }

    private MainActivity startActivity() {
        return activityRule.launchActivity(null);
    }
}