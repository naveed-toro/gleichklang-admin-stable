UPDATE user_settings
SET generalTermsAccepted = FALSE
WHERE generalTermsAccepted IS NULL;

UPDATE user_settings
SET privacyPolicyAccepted = FALSE
WHERE privacyPolicyAccepted IS NULL;

UPDATE user_settings
SET cancellationPolicyAccepted = FALSE
WHERE cancellationPolicyAccepted IS NULL;

UPDATE user_settings
SET disableAds = FALSE
WHERE disableAds IS NULL;

UPDATE user_settings
SET disableInfoMails = FALSE
WHERE disableInfoMails IS NULL;

UPDATE user_settings
SET disableRecommendationNotifications = FALSE
WHERE disableRecommendationNotifications IS NULL;

UPDATE user_settings
SET disableCipherMessageNotifications = FALSE
WHERE disableCipherMessageNotifications IS NULL;

UPDATE user_settings
SET disablePositiveRankingNotifications = FALSE
WHERE disablePositiveRankingNotifications IS NULL;

UPDATE user_settings
SET mailBlocked = FALSE
WHERE mailBlocked IS NULL;

UPDATE user_settings
SET talkToPress = FALSE
WHERE talkToPress IS NULL;

UPDATE user_settings
SET matchmakingSuccess = FALSE
WHERE matchmakingSuccess IS NULL;

UPDATE user_settings
SET communityRulesAccepted = FALSE
WHERE communityRulesAccepted IS NULL;