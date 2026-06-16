package com.idealink.vinty.utils

enum class FacebookEvent(val eventName: String) {
    AD_REWARD_EARNED("ad_reward_earned"),
    EARN_TICKET("earn_ticket"),
    REDEEM_ATTEMPT("redeem_attempt"),
    MISSION_PROGRESS_UP("mission_progress_up"),
    REGISTER("register"),
    LOGIN("login"),
    AD_WATCH_CLICKED("ad_watch_clicked")
}
