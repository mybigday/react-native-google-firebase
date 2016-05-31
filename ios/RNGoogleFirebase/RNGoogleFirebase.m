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
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(initialize){
    [FIRApp configure];
    fir = [[FIRDatabase database] reference];
    [[[fir child:@"test"] child:@"hello_world"] setValue:@{@"username": @"pepper"}];
}

@end
