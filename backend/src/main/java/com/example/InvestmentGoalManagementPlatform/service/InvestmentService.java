package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentDTO;
import com.example.InvestmentGoalManagementPlatform.entity.*;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import com.example.InvestmentGoalManagementPlatform.repository.AssetRepository;
import com.example.InvestmentGoalManagementPlatform.repository.InvestmentPlanRepository;
import com.example.InvestmentGoalManagementPlatform.repository.InvestmentRepository;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import com.example.InvestmentGoalManagementPlatform.utility.HelperUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final InvestmentPlanRepository investmentPlanRepository;
    private final AssetRepository assetRepository;
    private final StreakService streakService;

    public InvestmentService(
            InvestmentRepository investmentRepository,
            UserRepository userRepository,
            InvestmentPlanRepository investmentPlanRepository,
            AssetRepository assetRepository,
            StreakService streakService
    ) {
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
        this.investmentPlanRepository = investmentPlanRepository;
        this.assetRepository = assetRepository;
        this.streakService = streakService;
    }

    @Transactional
    public void completeMonthlyInvestment(
            Integer userId,
            Integer planId
    ) {
        User user =
                userRepository.findByUserId(userId);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + userId
            );
        }

        InvestmentPlan plan =
                investmentPlanRepository
                        .findByIdAndIsActiveTrue(planId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investment plan not found with id: "
                                                + planId
                                )
                        );

        if (
                plan.getUser() == null ||
                        !plan.getUser().getId().equals(user.getId())
        ) {
            throw new IllegalArgumentException(
                    "Investment plan does not belong to this user"
            );
        }

        if (
                plan.getAssetAllocations() == null ||
                        plan.getAssetAllocations().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Investment plan has no asset allocations"
            );
        }


        YearMonth currentMonth =
                YearMonth.now();

        LocalDateTime startOfCurrentMonth =
                currentMonth
                        .atDay(1)
                        .atStartOfDay();

        LocalDateTime startOfNextMonth =
                currentMonth
                        .plusMonths(1)
                        .atDay(1)
                        .atStartOfDay();

        Double alreadyInvested =
                investmentRepository
                        .sumMonthlyInvestmentByUserAndPlan(
                                userId,
                                planId,
                                startOfCurrentMonth,
                                startOfNextMonth
                        );

        if (
                alreadyInvested != null &&
                        alreadyInvested >=
                                plan.getMonthlyInvestmentAmount()
        ) {
            throw new IllegalArgumentException(
                    "This month's investment is already completed"
            );
        }

        for (
                PlanAllocation allocation :
                plan.getAssetAllocations()
        ) {
            double monthlyAmount =
                    plan.getMonthlyInvestmentAmount()
                            * allocation.getAllocationPercentage()
                            / 100.0;

            Investment investment =
                    new Investment();

            investment.setUser(user);
            investment.setInvestmentPlan(plan);
            investment.setAsset(
                    allocation.getAsset()
            );
            investment.setAmountInvested(
                    monthlyAmount
            );
            investment.setPurchaseDate(
                    LocalDate.now()
            );
            investment.setIsActive(true);

            investmentRepository.save(investment);
        }

        investmentRepository.flush();

        streakService.updateInvestmentStreak(
                user,
                plan
        );
    }
    @Transactional
    public Investment createFromPlan(
            User user,
            InvestmentPlan plan,
            Asset asset,
            Double monthlyAmount
    ) {
        Investment investment =
                new Investment();

        investment.setUser(user);
        investment.setInvestmentPlan(plan);
        investment.setAsset(asset);
        investment.setAmountInvested(monthlyAmount);
        investment.setIsActive(true);
        investment.setPurchaseDate(
                LocalDate.now()
        );

        return investmentRepository.save(investment);
    }

    @Transactional
    public InvestmentDTO createInvestment(
            InvestmentDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Investment data is required"
            );
        }

        if (dto.getUserId() == null) {
            throw new IllegalArgumentException(
                    "User id is required"
            );
        }

        if (dto.getPlanId() == null) {
            throw new IllegalArgumentException(
                    "Investment plan id is required"
            );
        }

        if (dto.getId() == null) {
            throw new IllegalArgumentException(
                    "Asset id is required"
            );
        }

        User user =
                userRepository.findByUserId(dto.getUserId());

        if (HelperUtility.isNull(user)) {
            throw new ResourceNotFoundException(
                    "User not found with id: "
                            + dto.getUserId()
            );
        }

        InvestmentPlan investmentPlan =
                investmentPlanRepository
                        .findByIdAndIsActiveTrue(
                                dto.getPlanId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investment plan not found with id: "
                                                + dto.getPlanId()
                                )
                        );

        Asset asset =
                assetRepository
                        .findById(dto.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Asset not found with id: "
                                                + dto.getId()
                                )
                        );

        if (Boolean.FALSE.equals(asset.getIsActive())) {
            throw new ResourceNotFoundException(
                    "Asset is not active with id: "
                            + dto.getId()
            );
        }

        if (investmentPlan.getUser() == null
                || !investmentPlan.getUser()
                .getId()
                .equals(user.getId())) {

            throw new IllegalArgumentException(
                    "Investment plan does not belong to this user"
            );
        }

        Investment investment = dto.toEntity();

        investment.setUser(user);
        investment.setInvestmentPlan(investmentPlan);
        investment.setAsset(asset);
        investment.setIsActive(true);

        Investment savedInvestment =
                investmentRepository.save(investment);

        /*
         * Flush is important because the streak query must include
         * the investment that was just saved.
         */
        investmentRepository.flush();

        /*
         * Check whether the user has now reached the required
         * monthly investment amount for this plan.
         */
        streakService.updateInvestmentStreak(
                user,
                investmentPlan
        );

        return InvestmentDTO.fromEntity(
                savedInvestment
        );
    }

    @Transactional(readOnly = true)
    public InvestmentDTO getInvestmentById(
            Integer investmentId
    ) {
        Investment investment =
                investmentRepository
                        .findByIdAndIsActiveTrue(
                                investmentId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investment not found with id: "
                                                + investmentId
                                )
                        );

        return InvestmentDTO.fromEntity(investment);
    }

    @Transactional(readOnly = true)
    public List<InvestmentDTO> getInvestmentsByUserId(
            Integer userId
    ) {
        return InvestmentDTO.fromEntity(
                investmentRepository
                        .findByUserIdAndIsActiveTrue(
                                userId
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<InvestmentDTO> getInvestmentsByPlanId(
            Integer planId
    ) {
        return InvestmentDTO.fromEntity(
                investmentRepository
                        .findByInvestmentPlanIdAndIsActiveTrue(
                                planId
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<InvestmentDTO> getInvestmentsByAssetId(
            Integer assetId
    ) {
        return InvestmentDTO.fromEntity(
                investmentRepository
                        .findByAssetIdAndIsActiveTrue(
                                assetId
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<InvestmentDTO>
    getInvestmentsByUserIdAndPlanId(
            Integer userId,
            Integer planId
    ) {
        return InvestmentDTO.fromEntity(
                investmentRepository
                        .findByUserIdAndInvestmentPlanIdAndIsActiveTrue(
                                userId,
                                planId
                        )
        );
    }

    @Transactional
    public InvestmentDTO updateInvestment(
            Integer investmentId,
            InvestmentDTO dto
    ) {
        Investment investment =
                investmentRepository
                        .findByIdAndIsActiveTrue(
                                investmentId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investment not found with id: "
                                                + investmentId
                                )
                        );

        dto.applyTo(investment);

        if (dto.getId() != null) {
            Asset asset =
                    assetRepository
                            .findById(dto.getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Asset not found with id: "
                                                    + dto.getId()
                                    )
                            );

            if (Boolean.FALSE.equals(
                    asset.getIsActive()
            )) {
                throw new ResourceNotFoundException(
                        "Asset is not active with id: "
                                + dto.getId()
                );
            }

            investment.setAsset(asset);
        }

        if (dto.getPlanId() != null) {
            InvestmentPlan investmentPlan =
                    investmentPlanRepository
                            .findByIdAndIsActiveTrue(
                                    dto.getPlanId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Investment plan not found with id: "
                                                    + dto.getPlanId()
                                    )
                            );

            if (investmentPlan.getUser() == null
                    || !investmentPlan.getUser()
                    .getId()
                    .equals(investment.getUser().getId())) {

                throw new IllegalArgumentException(
                        "Investment plan does not belong to this user"
                );
            }

            investment.setInvestmentPlan(
                    investmentPlan
            );
        }

        Investment updatedInvestment =
                investmentRepository.save(investment);

        investmentRepository.flush();

        /*
         * Recheck the streak because changing the amount might
         * make the user reach the monthly requirement.
         */
        streakService.updateInvestmentStreak(
                updatedInvestment.getUser(),
                updatedInvestment.getInvestmentPlan()
        );

        return InvestmentDTO.fromEntity(
                updatedInvestment
        );
    }

    @Transactional
    public void deleteInvestment(
            Integer investmentId
    ) {
        Investment investment =
                investmentRepository
                        .findByIdAndIsActiveTrue(
                                investmentId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investment not found with id: "
                                                + investmentId
                                )
                        );

        investment.setIsActive(false);

        investmentRepository.save(investment);
    }
}