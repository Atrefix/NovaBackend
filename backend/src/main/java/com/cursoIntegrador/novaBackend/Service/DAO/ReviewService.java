package com.cursoIntegrador.novaBackend.Service.DAO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.DTO.Review.ReviewDTO;
import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;
import com.cursoIntegrador.novaBackend.Model.Entity.Reviews;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Repository.ReviewsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    @Autowired
    private ReviewsRepository reviewRepo;

    public List<ReviewDTO> getAllReviews() {
        List<Reviews> original = reviewRepo.findAll();
        List<ReviewDTO> dtoList = new ArrayList<>();

        for (Reviews reviewDTO : original) {
            ReviewDTO element = new ReviewDTO(reviewDTO);
            dtoList.add(element);
        }

        return dtoList;
    }

    public ReviewDTO saveReviewGuest(Reviews review) {
        review.setCuenta(null);
        review.setVerified(false);
        reviewRepo.save(review);

        ReviewDTO reviewDTO = new ReviewDTO(review);
        return reviewDTO;
    }

    public ReviewDTO saveReviewUser(Reviews review, CustomUserDetails userDetails) {
        Cuenta cuenta = userDetails.getCuenta();

        review.setCuenta(cuenta);
        review.setEmail(cuenta.getEmail());
        review.setNombre(cuenta.getAlias());
        review.setVerified(true);
        reviewRepo.save(review);

        ReviewDTO reviewDTO = new ReviewDTO(review);
        return reviewDTO;
    }

}
