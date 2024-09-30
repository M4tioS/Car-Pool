package com.carpool.car_pool.services;

import com.carpool.car_pool.controllers.dtos.RideOfferResponse;
import com.carpool.car_pool.controllers.dtos.RideRequestRequest;
import com.carpool.car_pool.controllers.dtos.RideRequestResponse;
import com.carpool.car_pool.repositories.RideOfferRepository;
import com.carpool.car_pool.repositories.RideRequestRepository;
import com.carpool.car_pool.repositories.UserRepository;
import com.carpool.car_pool.repositories.entities.RideRequestsEntity;
import com.carpool.car_pool.services.converters.RideRequestConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.carpool.car_pool.repositories.entities.RequestStatus.PENDING;

@Service
@RequiredArgsConstructor
public class RideRequestService {


    private final RideOfferRepository rideOfferRepository;
    private final UserRepository userRepository;
    private final RideRequestRepository rideRequestRepository;

    public Long createRideRequest(@Valid RideRequestRequest request, String userEmail) {
        var rideOffer = rideOfferRepository.findById(request.getRideOfferId())
                // TODO: Better exception handling RideOfferNotFoundException
                .orElseThrow(() -> new RuntimeException("Ride Offer Not Found"));

        var rideRequester = userRepository.findByEmail(userEmail)
                // TODO: Better exception handling UserNotFoundException
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        if (rideOffer.getAvailableSeats() <= 0) throw new RuntimeException("Ride Offer Not Available");

        var rideRequestEntity = RideRequestsEntity.builder()
                .rideOffer(rideOffer)
                .requester(rideRequester)
                .requestStatus(PENDING)
                .build();

        rideOffer.setAvailableSeats(rideOffer.getAvailableSeats() - 1);
        rideOfferRepository.save(rideOffer);


        return rideRequestRepository.save(rideRequestEntity).getId();


    }

    public List<RideRequestResponse> getRideOffers(String userEmail, Long rideOfferId) {
        var rideOffer = rideOfferRepository.findById(rideOfferId)
                // TODO: Global exception handler RideOfferNotFoundException
                .orElseThrow(() -> new RuntimeException("Ride Offer Not Found"));

        var provider = userRepository.findByEmail(userEmail)
                // TODO: Global exception handler RuntimeException
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        if (!rideOffer.getCreator().getId().equals(provider.getId())) {
            // TODO: Global exception handler UnauthorizedAccessException
            throw new RuntimeException("Ride Offer Not Found");
        }

        List<RideRequestResponse>
    }
}
