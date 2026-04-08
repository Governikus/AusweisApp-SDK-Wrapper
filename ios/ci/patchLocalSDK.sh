#!/bin/sh

find AusweisApp2SDKWrapper -type f -name 'Package.swift' -exec sed -i "" -e "/url:/d" -e "s/\.exact\(.*\)/path: \"..\/AA2SwiftPackage\"/" {} \;

if [ -d "SDKWrapperTester" ]; then
	# Block definition: /<Pattern first line>/,/<Pattern last line>/
	sed -i "" -e '/D308B3092CBE7BED00A650F0.*=\ {/,/^\t\t};/c\
			D308B3092CBE7BED00A650F0 = {\
				isa = XCLocalSwiftPackageReference;\
				relativePath = ../AA2SwiftPackage;\
			};' \
	SDKWrapperTester/SDKWrapperTester.xcodeproj/project.pbxproj
fi
