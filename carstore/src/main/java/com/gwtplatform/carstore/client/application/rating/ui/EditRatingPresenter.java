/*
 * Copyright 2013 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gwtplatform.carstore.client.application.rating.ui;

import java.util.List;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.carstore.client.application.event.DisplayMessageEvent;
import com.gwtplatform.carstore.client.application.rating.event.RatingAddedEvent;
import com.gwtplatform.carstore.client.application.rating.ui.EditRatingPresenter.MyView;
import com.gwtplatform.carstore.client.application.widget.message.Message;
import com.gwtplatform.carstore.client.application.widget.message.MessageStyle;
import com.gwtplatform.carstore.client.resources.EditRatingMessages;
import com.gwtplatform.carstore.client.util.AbstractRestCallback;
import com.gwtplatform.carstore.client.util.ErrorHandlerAsyncCallback;
import com.gwtplatform.carstore.shared.api.CarsResource;
import com.gwtplatform.carstore.shared.api.RatingResource;
import com.gwtplatform.carstore.shared.dto.CarDto;
import com.gwtplatform.carstore.shared.dto.RatingDto;
import com.gwtplatform.dispatch.rest.delegates.client.ResourceDelegate;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class EditRatingPresenter extends PresenterWidget<MyView> implements EditRatingUiHandlers {
    public interface MyView extends PopupView, HasUiHandlers<EditRatingUiHandlers> {
        void edit(RatingDto ratingDto);

        void setAllowedCars(List<CarDto> carDtos);
    }

    private final ResourceDelegate<CarsResource> carsDelegate;
    private final ResourceDelegate<RatingResource> ratingDelegate;
    private final EditRatingMessages messages;

    @Inject
    EditRatingPresenter(
            EventBus eventBus,
            MyView view,
            ResourceDelegate<CarsResource> carsDelegate,
            ResourceDelegate<RatingResource> ratingDelegate,
            EditRatingMessages messages) {
        super(eventBus, view);

        this.carsDelegate = carsDelegate;
        this.ratingDelegate = ratingDelegate;
        this.messages = messages;

        getView().setUiHandlers(this);
    }

    @Override
    public void createNew() {
        reveal();
    }

    @Override
    public void onCancel() {
        getView().hide();
    }

    @Override
    public void onSave(RatingDto ratingDto) {
        ratingDelegate
                .withCallback(new ErrorHandlerAsyncCallback<RatingDto>(this) {
                    @Override
                    public void onSuccess(RatingDto savedRating) {
                        DisplayMessageEvent.fire(EditRatingPresenter.this, new Message(messages.ratingSaved(),
                                MessageStyle.SUCCESS));
                        RatingAddedEvent.fire(EditRatingPresenter.this, savedRating);
                        getView().hide();
                    }
                })
                .saveOrCreate(ratingDto);
    }

    private void reveal() {
        carsDelegate
                .withCallback((AbstractRestCallback<List<CarDto>>) this::onGetCarsSuccess)
                .getCars(null, 0, CarsResource.LIMIT_ALL);
    }

    private void onGetCarsSuccess(List<CarDto> carDtos) {
        getView().setAllowedCars(carDtos);
        getView().edit(new RatingDto());
        RevealRootPopupContentEvent.fire(this, this);
    }
}
