package io.cloudflight.gradle

import java.time.OffsetDateTime
import javax.persistence.*

@Entity
class SampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    val createdDate: OffsetDateTime = OffsetDateTime.now()
}