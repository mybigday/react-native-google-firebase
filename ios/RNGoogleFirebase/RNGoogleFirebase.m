//
//  RNGoogleFirebase.m
//  RNGoogleFirebase
//
//  Created by 嚴 孝頤 on 2016/5/31.
//  Copyright © 2016年 FuGood. All rights reserved.
//

#import "RNGoogleFirebase.h"

@implementation RNGoogleFirebase{
    FIRDatabaseReference *fir;
    FIRUser *currentUser;
}

alreadyConfiqure = NO;

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(configure){
    NSDictionary *allApps = [FIRApp allApps];
    if(allApps != nil){
        for(FIRApp *app in allApps.objectEnumerator){
            NSLog(@"App : %@", app);
            NSLog(@"App name: %@", app.name);
            [app deleteApp:^(BOOL success){
                NSLog(@"Delete app success!");
            }];
        }
    }
    [FIRApp configure];
}

RCT_EXPORT_METHOD(createUserWithEmail: (NSString *)email password: (NSString *)password resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    [[FIRAuth auth] createUserWithEmail:email password:password completion:^(FIRUser *_Nullable user, NSError *_Nullable error){
        if(error != NULL){
            NSLog(@"########## ErRoR: %@", [error description]);
            reject(@"create_user_fail", error.description, error);
        }
        if(user){
            currentUser = user;
            NSLog(@"%@", user.email);
//            resolve(@{});
            resolve(@{
                       @"anonymous": [NSNumber numberWithBool: currentUser.anonymous],
                       @"emailVerified": [NSNumber numberWithBool: currentUser.emailVerified],
                       @"refreshToken": currentUser.refreshToken?: @"",
                       @"providerID": currentUser.providerID?: @"",
                       @"uid": currentUser.uid?: @"",
                       @"displayName": currentUser.displayName?: @"",
                       @"photoURL": currentUser.photoURL?: @"",
                       @"email": currentUser.email?: @"",
                       });
        }
    }];
}

@end
