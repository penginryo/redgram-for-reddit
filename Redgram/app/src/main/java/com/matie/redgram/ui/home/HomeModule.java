package com.matie.redgram.ui.home;

import com.matie.redgram.data.managers.presenters.HomePresenter;
import com.matie.redgram.data.managers.presenters.HomePresenterImpl;
import com.matie.redgram.ui.App;
import com.matie.redgram.ui.FragmentScope;
import com.matie.redgram.ui.home.views.HomeView;

import dagger.Module;
import dagger.Provides;

/**
 * Created by matie on 09/06/15.
 */
@Module
public class HomeModule {

    private HomeView homeView;

    public HomeModule(HomeView homeView) {
        this.homeView = homeView;
    }

    @FragmentScope
    @Provides
    public HomeView provideView(){return homeView;}

    @FragmentScope
    @Provides
    public HomePresenter provideHomePresenter(App app){
        return new HomePresenterImpl(homeView, app);
    }

}
