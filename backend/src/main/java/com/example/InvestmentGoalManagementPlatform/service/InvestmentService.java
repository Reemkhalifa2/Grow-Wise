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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class InvestmentService {

    // Fractional units need more precision than money does, e.g. 100/52
    // OMR must keep enough digits that repeated monthly purchases don't
    // drift when summed back up.
    private static final int UNITS_SCALE = 6;

    private static final DateTimeFormatter MONTH_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final InvestmentPlanRepository investmentPlanRepository;
    private final AssetRepository assetRepository;
    private final StreakService streakService;

    /**
     * Whether a plan's required monthly amount has already been invested
     * for the current calendar month, and the month a new contribution
     * would count toward.
     */
    public record MonthlyStatus(
            boolean completed,
            String nextInvestmentMonthLabel
    ) {}

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


        if (computeMonthlyStatus(plan).completed()) {
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

            Asset asset = allocation.getAsset();

            Investment investment =
                    new Investment();

            investment.setUser(user);
            investment.setInvestmentPlan(plan);
            investment.setAsset(asset);
            investment.setAmountInvested(
                    monthlyAmount
            );
            investment.setPurchaseDate(
                    LocalDate.now()
            );

            // Freeze this month's price so past purchases are never
            // re-priced using a later, unrelated asset price.
            Double priceAtPurchase = asset.getCurrentPrice();
            investment.setPurchasePrice(priceAtPurchase);
            investment.setUnitsPurchased(
                    calculateUnits(monthlyAmount, priceAtPurchase)
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

        Double priceAtPurchase = asset.getCurrentPrice();
        investment.setPurchasePrice(priceAtPurchase);
        investment.setUnitsPurchased(
                calculateUnits(monthlyAmount, priceAtPurchase)
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

        // Units are always derived from the recorded purchase price rather
        // than trusted from the client, so the two can never drift apart.
        investment.setUnitsPurchased(
                calculateUnits(
                        investment.getAmountInvested(),
                        investment.getPurchasePrice()
                )
        );

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

        return mapWithMonthlyStatus(
                List.of(savedInvestment)
        ).get(0);
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

        return mapWithMonthlyStatus(List.of(investment))
                .get(0);
    }

    @Transactional(readOnly = true)
    public List<InvestmentDTO> getInvestmentsByUserId(
            Integer userId
    ) {
        return mapWithMonthlyStatus(
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
        return mapWithMonthlyStatus(
                investmentRepository
                        .findByInvestmentPlanIdAndIsActiveTrue(
                                planId
                        )
        );
    }

    /**
     * Exposes the same monthly-completion check used when mapping
     * investments, for callers (e.g. plan summary screens) that need it
     * for a plan that may not have any investments yet.
     */
    @Transactional(readOnly = true)
    public MonthlyStatus getMonthlyStatusForPlan(
            InvestmentPlan plan
    ) {
        return computeMonthlyStatus(plan);
    }

    @Transactional(readOnly = true)
    public List<InvestmentDTO> getInvestmentsByAssetId(
            Integer assetId
    ) {
        return mapWithMonthlyStatus(
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
        return mapWithMonthlyStatus(
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

        investment.setUnitsPurchased(
                calculateUnits(
                        investment.getAmountInvested(),
                        investment.getPurchasePrice()
                )
        );

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

        return mapWithMonthlyStatus(
                List.of(updatedInvestment)
        ).get(0);
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

    /**
     * Deactivates every active investment under a plan. Called when a plan
     * is soft-deleted so its investments stop being returned by the
     * user/investment listing queries — otherwise a deleted plan would be
     * rebuilt right back on the client from investments that are still active.
     */
    @Transactional
    public void deactivateInvestmentsByPlanId(
            Integer planId
    ) {
        List<Investment> investments =
                investmentRepository
                        .findByInvestmentPlanIdAndIsActiveTrue(
                                planId
                        );

        for (Investment investment : investments) {
            investment.setIsActive(false);
        }

        investmentRepository.saveAll(investments);
    }

    /**
     * amountInvested / purchasePrice, rounded for storage. Returns null
     * when either input is missing so callers can tell "not priced yet"
     * apart from a genuine zero.
     */
    private BigDecimal calculateUnits(
            Double amountInvested,
            Double purchasePrice
    ) {
        if (
                amountInvested == null ||
                        purchasePrice == null ||
                        purchasePrice <= 0
        ) {
            return null;
        }

        return BigDecimal.valueOf(amountInvested)
                .divide(
                        BigDecimal.valueOf(purchasePrice),
                        UNITS_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    /**
     * Reuses the same "amount invested this calendar month" query that
     * completeMonthlyInvestment() enforces, so a plan can never be reported
     * as completed here while still accepting another contribution there.
     */
    private MonthlyStatus computeMonthlyStatus(
            InvestmentPlan plan
    ) {
        if (
                plan == null ||
                        plan.getUser() == null ||
                        plan.getMonthlyInvestmentAmount() == null ||
                        plan.getMonthlyInvestmentAmount() <= 0
        ) {
            return new MonthlyStatus(false, null);
        }

        YearMonth currentMonth = YearMonth.now();

        Double investedThisMonth =
                investmentRepository
                        .sumMonthlyInvestmentByUserAndPlan(
                                plan.getUser().getId(),
                                plan.getId(),
                                currentMonth.atDay(1),
                                currentMonth.plusMonths(1).atDay(1)
                        );

        boolean completed =
                investedThisMonth != null &&
                        investedThisMonth >=
                                plan.getMonthlyInvestmentAmount();

        YearMonth nextInvestmentMonth =
                completed
                        ? currentMonth.plusMonths(1)
                        : currentMonth;

        return new MonthlyStatus(
                completed,
                nextInvestmentMonth.format(MONTH_LABEL_FORMAT)
        );
    }

    /**
     * Maps investments to DTOs and stamps each one with its plan's monthly
     * completion status, computing that status once per distinct plan
     * rather than once per investment record.
     */
    private List<InvestmentDTO> mapWithMonthlyStatus(
            List<Investment> investments
    ) {
        List<InvestmentDTO> dtos =
                InvestmentDTO.fromEntity(investments);

        applyMonthlyStatus(investments, dtos);

        return dtos;
    }

    private void applyMonthlyStatus(
            List<Investment> investments,
            List<InvestmentDTO> dtos
    ) {
        Map<Integer, MonthlyStatus> statusByPlanId =
                new HashMap<>();

        for (int i = 0; i < investments.size(); i++) {
            InvestmentPlan plan =
                    investments.get(i).getInvestmentPlan();

            if (plan == null || plan.getId() == null) {
                continue;
            }

            MonthlyStatus status =
                    statusByPlanId.computeIfAbsent(
                            plan.getId(),
                            id -> computeMonthlyStatus(plan)
                    );

            dtos.get(i).setMonthlyInvestmentCompleted(
                    status.completed()
            );

            dtos.get(i).setNextInvestmentMonth(
                    status.nextInvestmentMonthLabel()
            );
        }
    }
}