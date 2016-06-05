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
	FIRDatabase *defaultDatabase;
	FIRDatabaseReference *defaultDatabaseRootReference;
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
	NSDictionary *allApps = [FIRApp allApps];
	
	if(!allApps || ![allApps objectForKey:defaultAppKey]){
		[FIRApp configure];
		allApps = [FIRApp allApps];
    }
	NSArray *result = @[];
	for(NSString *key in allApps.allKeys){
		result = [result arrayByAddingObject:@{ @"key": key }];
		[appDict setValue:[NSMutableDictionary dictionaryWithObject:[allApps objectForKey:key] forKey:@"FIRApp"] forKey:key];
	}
	resolve(result);
	
//    NSDictionary *userInfo = [NSDictionary dictionaryWithObject:@"Firebase configure error." forKey:NSLocalizedDescriptionKey];
//    NSError *error = [NSError errorWithDomain:@"firebase_config_error" code:-101 userInfo:userInfo];
//    reject(error.domain, error.localizedDescription, error);

//    rootDatabaseRef = [[FIRDatabase database] reference];
//    dataBaseRefList = [[NSMutableArray alloc] init];
}


// ====== Auth Group ======

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

RCT_EXPORT_METHOD(signInWithEmail: (NSString *)email password:(NSString *)password resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
	[defaultAuth signInWithEmail:email password:password completion:^(FIRUser * _Nullable user, NSError * _Nullable error) {
		if(error != NULL){
			reject(@"sign_in_fail", error.description, error);
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

RCT_EXPORT_METHOD(signOut:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
	NSError *error;
	[defaultAuth signOut:&error];
	if(error){
		reject(@"sign_out_fail", error.description, error);
		return;
	}
	resolve(@{});
}

RCT_EXPORT_METHOD(sendCurrentUserEmailVerification:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
	[currentUser sendEmailVerificationWithCompletion:^(NSError * _Nullable error) {
		reject(@"sign_out_fail", error.description, error);
		return;
	}];
	resolve(@{});
}

// ====== Database Group ======

RCT_EXPORT_METHOD(databaseAndReference:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
	defaultDatabase = [FIRDatabase database];
	[[appDict objectForKey:defaultAppKey] setValue:defaultDatabase forKey:@"FIRDatabase"];
	defaultDatabaseRootReference = [defaultDatabase reference];
	NSString *rootReferenceKey = @"DatabaseReference:/";
	
	[[appDict objectForKey:defaultAppKey] setValue:defaultDatabaseRootReference forKey:rootReferenceKey];
	
	resolve(@{
			  @"appKey": defaultAppKey,
			  @"referenceKey":rootReferenceKey,
			  @"path": @"/"
			  });
}

RCT_EXPORT_METHOD(childFromReference: (NSString *)referenceKey path:(NSString *)path resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    FIRDatabaseReference *source = [[appDict objectForKey:defaultAppKey] objectForKey:referenceKey];
    FIRDatabaseReference *target = [source child:path];
    NSString *newKey = [referenceKey stringByAppendingString:path];
    
    [[appDict objectForKey:defaultAppKey] setValue:target forKey:newKey];
    resolve(@{
              @"appKey": defaultAppKey,
              @"referenceKey":newKey,
              @"path": [newKey stringByReplacingOccurrencesOfString:@"DatabaseReference:" withString:@""]
              });
}

RCT_EXPORT_METHOD(setValueForReference: (NSString *)referenceKey value:(NSDictionary *)value){
    FIRDatabaseReference *source = [[appDict objectForKey:defaultAppKey] objectForKey:referenceKey];
    [source setValue:value];
}

@end
