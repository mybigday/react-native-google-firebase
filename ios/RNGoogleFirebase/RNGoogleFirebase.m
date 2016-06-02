//
//  RNGoogleFirebase.m
//  RNGoogleFirebase
//
//  Created by 嚴 孝頤 on 2016/5/31.
//  Copyright © 2016年 FuGood. All rights reserved.
//

#import "RNGoogleFirebase.h"

@implementation RNGoogleFirebase{
    NSString *defaultAppKey;
    NSDictionary *appDict;
    FIRApp *dafaultApp;
    FIRAuth *defaultAuth;
    FIRUser *currentUser;
    FIRDatabaseReference *rootDatabaseRef;
    NSMutableArray *dataBaseRefList;
}

- (id)init {
    self = [super init];
    if (self){
        defaultAppKey = @"__FIRAPP_DEFAULT";
        appDict = [[NSMutableDictionary alloc] init];
    }
    return self;
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(configure: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    [FIRApp configure];
    NSDictionary *allApps = [FIRApp allApps];
    if(allApps != nil){
        NSArray *result = @[];
        for(NSString *key in allApps.allKeys){
            result = [result arrayByAddingObject:@{ @"key": key }];
            [appDict setValue:[NSMutableDictionary dictionaryWithObject:[allApps objectForKey:@"FIRApp"] forKey:key] forKey:key];
        }
        resolve(result);
        return;
    }
    NSDictionary *userInfo = [NSDictionary dictionaryWithObject:@"Firebase configure error." forKey:NSLocalizedDescriptionKey];
    NSError *error = [NSError errorWithDomain:@"firebase_config_error" code:-101 userInfo:userInfo];
    reject(error.domain, error.localizedDescription, error);

//    rootDatabaseRef = [[FIRDatabase database] reference];
//    dataBaseRefList = [[NSMutableArray alloc] init];
}

RCT_EXPORT_METHOD(auth: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    defaultAuth = [FIRAuth auth];
    [[appDict objectForKey:defaultAppKey] setValue:defaultAuth forKey:@"FIRAuth"];
    resolve(@{
              @"key": defaultAppKey
              });
}

RCT_EXPORT_METHOD(createUserWithEmail: (NSString *)email password: (NSString *)password resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    [defaultAuth createUserWithEmail:email password:password completion:^(FIRUser *_Nullable user, NSError *_Nullable error){
        if(error != NULL){
            NSLog(@"########## ErRoR: %@", [error description]);
            reject(@"create_user_fail", error.description, error);
        }
        if(user){
            currentUser = user;
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
