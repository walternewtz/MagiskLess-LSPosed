/*
 * This file is part of LFPosed.
 *
 * LFPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LFPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LFPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2021 LFPosed Contributors
 */

plugins {
    alias(libs.plugins.agp.lib)
}

android {
    buildFeatures {
        aidl = true
    }

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    namespace = "org.lsposed.lspd.managerservice"
}

dependencies {
    api(libs.rikkax.parcelablelist)
}
