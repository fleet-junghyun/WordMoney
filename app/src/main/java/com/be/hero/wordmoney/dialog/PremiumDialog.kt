package com.be.hero.wordmoney.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.be.hero.wordmoney.config.WordMoneyConfig
import com.be.hero.wordmoney.databinding.DialogPremiumBinding

class PremiumDialog : CompatBottomSheetDialog() {

    private var _binding: DialogPremiumBinding? = null
    private val binding get() = _binding!!

    private var billingClient: BillingClient? = null
    private var removeAdProduct: ProductDetails? = null

    private val config by lazy {
        WordMoneyConfig.get(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPremiumBinding.inflate(inflater, container, false)

        initBilling()

        binding.apply {
            exit.setOnClickListener { dismiss() }
            btnPurchase.setOnClickListener {
                removeAdProduct?.let { launchBillingFlow(it) }
            }
        }

        return binding.root
    }


    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun initBilling() {
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadProductDetails()
                }
            }
        })
    }

    private fun loadProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(ITEM_REMOVE_AD_ALWAYS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                removeAdProduct = productDetailsList.find { it.productId == ITEM_REMOVE_AD_ALWAYS }
            }
        }
    }

    private fun launchBillingFlow(productDetails: ProductDetails) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient?.launchBillingFlow(requireActivity(), billingFlowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        progressPurchase(purchase)
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient?.consumeAsync(params) { _, _ -> }
    }

    private fun progressPurchase(purchase: Purchase) {
        if (purchase.products.contains(ITEM_REMOVE_AD_ALWAYS)) {
            config.isPremium = true
            dismiss()
        }
    }

    override fun onDestroy() {
        billingClient?.endConnection()
        super.onDestroy()
    }

    companion object {
        const val ITEM_REMOVE_AD_ALWAYS = "remove_ad_always"
    }

}